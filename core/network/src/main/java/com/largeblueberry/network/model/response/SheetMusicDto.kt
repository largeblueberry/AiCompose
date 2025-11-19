package com.largeblueberry.network.model.response

data class SheetMusicDto(
    val midiUrl: String?, // null 가능성을 열어두는 것이 안전합니다.
    val scoreUrl: String?
)