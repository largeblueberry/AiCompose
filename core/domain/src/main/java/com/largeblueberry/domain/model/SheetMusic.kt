package com.largeblueberry.domain.model

data class SheetMusic(
    val id: String,
    val title: String,
    val composer: String?,
    val scoreUrl: String,
    val midiUrl: String,
    val createdAt: String,
    val duration: Int?,
    val key: String?,
    val tempo: Int?
)
