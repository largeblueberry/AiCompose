package com.largeblueberry.record.ui

/** 화면 상단 문구용 상태. 박·카운트인 같은 실시간 정보는 RecordUiState에 있다. */
enum class RecordingState {
    WAITING,
    COUNT_IN,
    RECORDING,
    COMPLETED,
    FAILED_START,
    FAILED_STOP,
    TOO_SHORT,
    /** 화면을 벗어나 녹음을 버렸다 */
    CANCELLED,
    /** 전화 등 다른 앱이 오디오를 가져가 녹음이 중단됐다 */
    INTERRUPTED,
}
