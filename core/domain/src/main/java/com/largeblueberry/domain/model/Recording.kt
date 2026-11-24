package com.largeblueberry.domain.model

data class RecordingConfig(
    val bpm: Int,
    val beatsPerBar: Int = 4,
    /** 녹음 전에 들려주는 카운트인 박 수. 이 구간은 파일에 쓰지 않는다. */
    val countInBeats: Int = 4,
) {
    init {
        require(bpm in Tempo.MIN_BPM..Tempo.MAX_BPM) { "bpm out of range: $bpm" }
        require(beatsPerBar > 0 && countInBeats >= 0)
    }
}

/** 녹음기 상태. beat는 마디 안 박 위치(0부터). */
sealed interface RecorderState {
    data object Idle : RecorderState

    /** 카운트인 중. beatsLeft = 녹음 시작까지 남은 박 수(첫 박에서 countInBeats). */
    data class CountIn(val beatsLeft: Int, val beat: Int, val clickAudible: Boolean) : RecorderState

    /**
     * 녹음 중. clickAudible = 녹음 중에도 클릭 소리가 나는지(이어폰이 연결됐을 때만 true).
     * 소리가 없으면 화면으로 박을 보여 준다.
     */
    data class Recording(val beat: Int, val elapsedMs: Long, val clickAudible: Boolean) : RecorderState

    /** 녹음 도중 실패(캡처 오류, 다른 앱이 오디오를 가져감). stopRecording을 불러 원인을 받고 정리한다. */
    data object Failed : RecorderState
}
