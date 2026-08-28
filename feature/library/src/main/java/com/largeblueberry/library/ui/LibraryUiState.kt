package com.largeblueberry.library.ui


import com.largeblueberry.library.domainLayer.model.LibraryModel
import com.largeblueberry.network.model.request.UploadState

data class LibraryUiState(
    val audioRecords: List<LibraryModel> = emptyList(),
    val isEmpty: Boolean = true,
    val deleteResult: Result<Unit>? = null,
    val uploadState: UploadState = UploadState(),
    val uploadingRecordId: Int? = null,
    val showRenameDialogForRecord: LibraryModel? = null,
    val currentPlayingRecordId: Int? = null,
    val isPlaying: Boolean = false,
    val isUploadingInProgress: Boolean = false,
    val showUploadInProgressMessage: Boolean = false,
    val currentUploads: Int? = null,
    val maxUploads: Int? = null
)