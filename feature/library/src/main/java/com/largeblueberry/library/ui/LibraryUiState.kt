package com.largeblueberry.library.ui

import com.largeblueberry.domain.model.LibraryModel
import com.largeblueberry.library.ui.model.RecordTranscription

data class LibraryUiState(
    val audioRecords: List<LibraryModel> = emptyList(),
    val isEmpty: Boolean = true,
    val deleteResult: Result<Unit>? = null,
    /** 녹음 id → 채보 상태. 없으면 [RecordTranscription.Idle]. */
    val transcriptions: Map<Int, RecordTranscription> = emptyMap(),
    val showRenameDialogForRecord: LibraryModel? = null,
    val currentPlayingRecordId: Int? = null,
    val isPlaying: Boolean = false,
) {
    fun transcriptionOf(recordId: Int): RecordTranscription = transcriptions[recordId] ?: RecordTranscription.Idle
}
