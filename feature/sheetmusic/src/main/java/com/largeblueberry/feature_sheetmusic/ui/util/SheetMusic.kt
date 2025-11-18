package com.largeblueberry.feature_sheetmusic.ui.util

data class SheetMusic(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val createdDate: String,
    val midiUrl: String? = null,
    val scoreUrl: String? = null
)