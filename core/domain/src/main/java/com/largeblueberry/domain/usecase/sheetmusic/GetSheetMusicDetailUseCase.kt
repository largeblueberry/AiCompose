package com.largeblueberry.domain.usecase.sheetmusic

import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.repository.SheetMusicRepository
import javax.inject.Inject

/** 악보 정보와 MusicXML 본문. 없으면 null. */
class GetSheetMusicDetailUseCase @Inject constructor(
    private val repository: SheetMusicRepository
) {
    data class Detail(val sheetMusic: SheetMusic, val musicXml: String)

    suspend operator fun invoke(id: String): Result<Detail?> = runCatching {
        val score = repository.getScore(id) ?: return@runCatching null
        Detail(score, repository.readMusicXml(score).getOrThrow())
    }
}
