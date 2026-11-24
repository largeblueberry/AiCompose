package com.largeblueberry.library.ui.viemodel

import com.largeblueberry.domain.model.LibraryModel
import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.model.TranscriptionJob
import com.largeblueberry.domain.model.TranscriptionStage
import com.largeblueberry.domain.repository.LibraryRepository
import com.largeblueberry.domain.repository.SheetMusicRepository
import com.largeblueberry.domain.repository.TranscriptionScheduler
import com.largeblueberry.domain.usecase.library.DeleteAudioRecordUseCase
import com.largeblueberry.domain.usecase.library.GetAudioRecordsUseCase
import com.largeblueberry.domain.usecase.library.RenameAudioRecordUseCase
import com.largeblueberry.domain.usecase.sheetmusic.EnqueueTranscriptionUseCase
import com.largeblueberry.domain.usecase.sheetmusic.GetScoresUseCase
import com.largeblueberry.domain.usecase.sheetmusic.ObserveTranscriptionsUseCase
import com.largeblueberry.library.ui.model.RecordTranscription
import com.largeblueberry.library.util.AudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * 라이브러리 ViewModel. 채보 등록은 스케줄러로 넘기고, 화면 상태는 스케줄러·악보 두 흐름에서만 나온다.
 * 진짜 유스케이스에 가짜 저장소·스케줄러를 끼운다. Robolectric은 AudioPlayer가 받는 Context 때문에만 쓴다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LibraryViewModelTest {

    private val records = MutableStateFlow(listOf(record(1), record(2)))
    private val jobs = MutableStateFlow<Map<Int, TranscriptionJob>>(emptyMap())
    private val scores = MutableStateFlow<List<SheetMusic>>(emptyList())
    private val enqueued = mutableListOf<Int>()
    private val events = mutableListOf<String>()

    private val library = object : LibraryRepository {
        override fun getAllRecords(): Flow<List<LibraryModel>> = records
        override suspend fun deleteRecord(record: LibraryModel) {
            events += "delete ${record.id}"
            records.value = records.value.filterNot { it.id == record.id }
        }
        override suspend fun renameRecord(record: LibraryModel) = Unit
    }

    private val scheduler = object : TranscriptionScheduler {
        override fun enqueue(recordId: Int) {
            enqueued += recordId
        }
        override fun cancel(recordId: Int) {
            events += "cancel $recordId"
        }
        override fun observeJobs(): Flow<Map<Int, TranscriptionJob>> = jobs
    }

    private val sheetMusic = object : SheetMusicRepository {
        override fun getAllScores(): Flow<List<SheetMusic>> = scores
        override suspend fun getScore(id: String): SheetMusic? = null
        override suspend fun readMusicXml(score: SheetMusic): Result<String> = Result.failure(UnsupportedOperationException())
        override suspend fun deleteScore(score: SheetMusic): Result<Unit> = Result.success(Unit)
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = LibraryViewModel(
        getAudioRecordsUseCase = GetAudioRecordsUseCase(library),
        deleteAudioRecordUseCase = DeleteAudioRecordUseCase(library, scheduler),
        renameAudioRecordUseCase = RenameAudioRecordUseCase(library),
        enqueueTranscriptionUseCase = EnqueueTranscriptionUseCase(scheduler),
        observeTranscriptionsUseCase = ObserveTranscriptionsUseCase(scheduler),
        getScoresUseCase = GetScoresUseCase(sheetMusic),
        audioPlayer = AudioPlayer(RuntimeEnvironment.getApplication()),
    )

    private fun LibraryViewModel.stateOf(id: Int) = uiState.value.transcriptionOf(id)

    @Test
    fun `채보를 누르면 스케줄러에 녹음 id만 넘기고, 상태는 작업 흐름에서 온다`() = runTest {
        val vm = viewModel()

        vm.transcribe(record(1))

        assertEquals(listOf(1), enqueued)
        assertEquals(RecordTranscription.Idle, vm.stateOf(1)) // 아직 작업 흐름이 아무것도 안 보냄

        jobs.value = mapOf(1 to TranscriptionJob.Queued)
        assertEquals(RecordTranscription.Queued, vm.stateOf(1))
    }

    @Test
    fun `진행 → 작업 끝(악보 아직) → 악보 도착, 저장 중을 거쳐 Done, 다른 녹음은 그대로`() = runTest {
        val vm = viewModel()

        jobs.value = mapOf(1 to TranscriptionJob.Running(TranscriptionStage.ANALYZING, 0.5f))
        assertEquals(RecordTranscription.Running(TranscriptionStage.ANALYZING, 0.5f), vm.stateOf(1))

        jobs.value = emptyMap()
        assertEquals(RecordTranscription.Saving, vm.stateOf(1))

        scores.value = listOf(score("s1", recordId = 1))
        assertEquals(RecordTranscription.Done("s1"), vm.stateOf(1))
        assertEquals(RecordTranscription.Idle, vm.stateOf(2))
    }

    @Test
    fun `화면을 다시 열어도(새 ViewModel) 같은 흐름에서 같은 상태가 나온다`() = runTest {
        jobs.value = mapOf(2 to TranscriptionJob.Running(TranscriptionStage.READING, 0.1f))
        scores.value = listOf(score("s1", recordId = 1))

        val vm = viewModel()

        assertEquals(RecordTranscription.Done("s1"), vm.stateOf(1))
        assertEquals(RecordTranscription.Running(TranscriptionStage.READING, 0.1f), vm.stateOf(2))
    }

    @Test
    fun `녹음을 지우면 채보 작업을 먼저 멈춘다`() = runTest {
        val vm = viewModel()

        vm.deleteRecord(record(1))

        assertEquals(listOf("cancel 1", "delete 1"), events)
        assertEquals(listOf(2), vm.uiState.value.audioRecords.map { it.id })
    }

    private companion object {
        fun record(id: Int) = LibraryModel(
            id = id,
            filename = "녹음 $id.wav",
            filePath = "/nonexistent/rec$id.wav",
            fileSize = 0,
            duration = "0:01",
            bpm = 100,
            sampleRate = 22_050,
        )

        fun score(id: String, recordId: Int) = SheetMusic(
            id = id, title = id, recordId = recordId, createdAt = 0, bpm = 100, beatsPerBar = 4,
            keyName = "C major", noteCount = 1, musicXmlPath = "", midiPath = "",
        )
    }
}
