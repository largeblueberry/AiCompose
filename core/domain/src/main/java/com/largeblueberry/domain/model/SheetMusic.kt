package com.largeblueberry.domain.model

/**
 * 기기 안에서 만든 악보. 파일은 앱 내부 저장소에 있다(서버 URL이 아니다).
 */
data class SheetMusic(
    val id: String,
    val title: String,
    /** 이 악보를 만든 녹음. 녹음이 지워졌으면 null. */
    val recordId: Int?,
    val createdAt: Long,
    val bpm: Int,
    val beatsPerBar: Int,
    /** 예: "C major", "F# minor" */
    val keyName: String,
    val noteCount: Int,
    val musicXmlPath: String,
    val midiPath: String,
)
