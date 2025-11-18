package com.largeblueberry.library.ui


import com.largeblueberry.library.domainLayer.model.LibraryModel
import com.largeblueberry.network.model.request.UploadState

data class LibraryUiState(
    val audioRecords: List<LibraryModel> = emptyList(),
    val isEmpty: Boolean = true,
    val deleteResult: Result<Unit>? = null,
    val uploadState: UploadState = UploadState(),
    val uploadingRecordId: Int? = null,
    val showRenameDialogForRecord: LibraryModel? = null, // 이름 변경 다이얼로그 표시 여부 및 대상 레코드
    val currentPlayingRecordId: Int? = null, // 현재 재생 중인 레코드 ID
    val isPlaying: Boolean = false, // 현재 오디오 재생 중인지 여부
    val isUploadingInProgress: Boolean = false, // 현재 업로드 작업이 진행 중인지 여부
    val showUploadInProgressMessage: Boolean = false, //  업로드 중 메시지 표시 여부
    val currentUploads: Int? = null,
    val maxUploads: Int? = null
)