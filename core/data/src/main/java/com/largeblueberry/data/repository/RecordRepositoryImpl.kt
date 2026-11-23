package com.largeblueberry.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.largeblueberry.data.audio.AudioOutputs
import com.largeblueberry.data.audio.AudioRecordDao
import com.largeblueberry.data.audio.Metronome
import com.largeblueberry.data.mapper.RecordMapper
import com.largeblueberry.domain.model.RecordModel
import com.largeblueberry.domain.model.RecorderState
import com.largeblueberry.domain.model.RecordingConfig
import com.largeblueberry.domain.repository.RecordRepository
import com.largeblueberry.domain.repository.RecordingInterruptedException
import com.largeblueberry.domain.repository.RecordingTooShortException
import com.largeblueberry.music.audio.Wav
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToLong

/**
 * 카운트인 + 16-bit PCM mono WAV 녹음(docs/map/notes.md "녹음").
 *
 * - 22,050Hz(모델 입력 레이트)를 먼저 시도하고, 기기가 거부하면 44,100Hz로 녹음한다(채보할 때 다운샘플).
 * - 마이크와 메트로놈을 함께 시작하고, 카운트인 길이만큼의 마이크 샘플은 버린다. 그래서 파일의 0초가
 *   (기기 지연을 빼면) 첫 마디 첫 박이다. 남는 지연은 채보의 위상 추정이 흡수한다.
 * - 박 표시는 마이크 샘플 수(녹음 시계)로 센다.
 * - 저장 위치는 filesDir/recordings. 캐시 디렉터리는 OS가 지울 수 있어 쓰지 않는다.
 * - 녹음하는 동안 오디오 포커스를 잡아 다른 앱의 재생을 멈춘다(그 소리가 마이크로 들어가지 않게).
 *   포커스를 잃거나(전화 등) 캡처가 실패하면 바로 [RecorderState.Failed]로 알린다. 정리는 stop이 한다.
 */
class RecordRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioRecordDao: AudioRecordDao,
) : RecordRepository {

    private val _state = MutableStateFlow<RecorderState>(RecorderState.Idle)
    override val state: StateFlow<RecorderState> = _state.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val lock = Any()
    private var session: Session? = null

    private class Session(
        val config: RecordingConfig,
        val file: File,
        val raf: RandomAccessFile,
        val record: AudioRecord,
        val metronome: Metronome,
        val sampleRate: Int,
        val clickAudible: Boolean,
        val focusRequest: AudioFocusRequest,
    ) {
        @Volatile var running = true
        @Volatile var writtenSamples = 0L
        @Volatile var error: Throwable? = null
        var thread: Thread? = null
    }

    override fun startRecording(config: RecordingConfig): Result<Unit> = synchronized(lock) {
        if (session != null) return Result.failure(IllegalStateException("already recording"))
        var record: AudioRecord? = null
        var raf: RandomAccessFile? = null
        var file: File? = null
        try {
            val (rec, rate) = openRecorder()
            record = rec
            val dir = File(context.filesDir, RECORDINGS_DIR).apply { mkdirs() }
            file = uniqueFile(dir)
            raf = RandomAccessFile(file, "rw").apply {
                setLength(0)
                write(Wav.header(rate, 0))
            }
            val clickAudible = AudioOutputs.headphonesConnected(context)
            val metronome = Metronome(config.bpm, config.beatsPerBar, config.countInBeats, clickAudible)
            lateinit var s: Session
            val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setOnAudioFocusChangeListener { change ->
                    if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        fail(s, RecordingInterruptedException())
                    }
                }
                .build()
            s = Session(config, file, raf, rec, metronome, rate, clickAudible, focus)
            // 포커스를 못 받아도(드묾) 녹음은 시도한다. 다른 앱 소리가 섞일 수 있다는 것뿐이다.
            if (audioManager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.w(TAG, "audio focus not granted")
            }

            _state.value = RecorderState.CountIn(config.countInBeats, 0, clickAudible)
            rec.startRecording()
            metronome.start()
            s.thread = Thread({ captureLoop(s) }, "recorder").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }
            session = s
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "start failed", e)
            record?.release()
            raf?.close()
            file?.delete()
            _state.value = RecorderState.Idle
            Result.failure(e)
        }
    }

    private fun captureLoop(s: Session) {
        val spb = s.sampleRate * 60.0 / s.config.bpm
        val countInSamples = (spb * s.config.countInBeats).roundToLong()
        val buf = ShortArray(s.sampleRate / 20) // 50ms
        val bytes = ByteArray(buf.size * 2)
        var total = 0L
        var lastBeat = -1L
        try {
            while (s.running) {
                val n = s.record.read(buf, 0, buf.size)
                // 멈추는 중(record.stop 뒤)에는 read가 오류 코드를 돌려줄 수 있다. 실패가 아니다.
                if (n < 0) {
                    if (!s.running) break
                    throw IllegalStateException("AudioRecord.read error $n")
                }
                if (n == 0) continue
                val start = total
                total += n
                // 카운트인 구간을 지난 샘플만 파일에 쓴다
                val skip = (countInSamples - start).coerceIn(0, n.toLong()).toInt()
                if (skip < n) {
                    var b = 0
                    for (i in skip until n) {
                        val v = buf[i].toInt()
                        bytes[b++] = (v and 0xFF).toByte()
                        bytes[b++] = (v shr 8 and 0xFF).toByte()
                    }
                    s.raf.write(bytes, 0, b)
                    s.writtenSamples += (n - skip)
                }
                val beat = (total / spb).toLong()
                if (beat != lastBeat) {
                    lastBeat = beat
                    _state.value = if (beat < s.config.countInBeats) {
                        RecorderState.CountIn(
                            beatsLeft = (s.config.countInBeats - beat).toInt(),
                            beat = (beat % s.config.beatsPerBar).toInt(),
                            clickAudible = s.clickAudible,
                        )
                    } else {
                        RecorderState.Recording(
                            beat = ((beat - s.config.countInBeats) % s.config.beatsPerBar).toInt(),
                            elapsedMs = s.writtenSamples * 1000 / s.sampleRate,
                            clickAudible = s.clickAudible,
                        )
                    }
                }
            }
        } catch (e: Throwable) {
            if (!s.running) return // 멈추는 중에 난 오류는 무시한다
            Log.e(TAG, "capture failed", e)
            fail(s, e)
        }
    }

    /** 녹음 도중 실패를 바로 알린다. 파일·자원 정리는 stopRecording/cancelRecording이 한다. */
    private fun fail(s: Session, e: Throwable) {
        if (s.error == null) s.error = e
        s.running = false
        _state.value = RecorderState.Failed
    }

    override fun stopRecording(): Result<RecordModel> {
        val s = synchronized(lock) { session.also { session = null } }
            ?: return Result.failure(IllegalStateException("not recording"))
        finish(s)
        s.error?.let {
            s.file.delete()
            return Result.failure(it)
        }
        if (s.writtenSamples == 0L) {
            s.file.delete()
            return Result.failure(RecordingTooShortException())
        }
        val durationMs = s.writtenSamples * 1000 / s.sampleRate
        return Result.success(
            RecordModel(
                filename = s.file.name,
                filePath = s.file.absolutePath,
                fileSize = s.file.length(),
                duration = formatDuration(durationMs),
                createdAt = System.currentTimeMillis(),
                bpm = s.config.bpm,
                beatsPerBar = s.config.beatsPerBar,
                sampleRate = s.sampleRate,
            )
        )
    }

    override fun cancelRecording() {
        val s = synchronized(lock) { session.also { session = null } } ?: return
        finish(s)
        s.file.delete()
    }

    /** 스레드를 멈추고 WAV 헤더의 크기를 채운다. */
    private fun finish(s: Session) {
        s.running = false
        s.metronome.stop()
        audioManager.abandonAudioFocusRequest(s.focusRequest)
        try {
            s.record.stop()
        } catch (_: IllegalStateException) {
        }
        s.thread?.join(1000)
        s.record.release()
        try {
            val dataBytes = (s.writtenSamples * 2).toInt()
            s.raf.seek(0)
            s.raf.write(Wav.header(s.sampleRate, dataBytes))
        } catch (e: Exception) {
            Log.e(TAG, "header update failed", e)
            if (s.error == null) s.error = e
        } finally {
            s.raf.close()
            _state.value = RecorderState.Idle
        }
    }

    override suspend fun saveRecord(recordModel: RecordModel) {
        audioRecordDao.insertRecord(RecordMapper.toEntity(recordModel))
    }

    /** 22,050Hz를 먼저, 안 되면 44,100Hz. 음성 인식용 입력(자동 이득·잡음 억제가 덜한 경로)을 먼저 쓴다. */
    @SuppressLint("MissingPermission") // 화면에서 권한을 확인한 뒤 부른다. 없으면 SecurityException → 실패로 돌려준다.
    private fun openRecorder(): Pair<AudioRecord, Int> {
        for (rate in intArrayOf(22_050, 44_100)) {
            val min = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            if (min <= 0) continue
            for (source in intArrayOf(MediaRecorder.AudioSource.VOICE_RECOGNITION, MediaRecorder.AudioSource.MIC)) {
                val r = AudioRecord(source, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(min, rate / 5 * 2))
                if (r.state == AudioRecord.STATE_INITIALIZED) return r to rate
                r.release()
            }
        }
        throw IllegalStateException("no supported AudioRecord configuration")
    }

    private fun uniqueFile(dir: File): File {
        val base = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        var f = File(dir, "$base.wav")
        var i = 1
        while (f.exists()) f = File(dir, "${base}_${i++}.wav")
        return f
    }

    private fun formatDuration(ms: Long): String {
        val sec = ms / 1000
        return String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60)
    }

    companion object {
        private const val TAG = "Recorder"
        const val RECORDINGS_DIR = "recordings"
    }
}
