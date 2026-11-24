package com.largeblueberry.feature_sheetmusic.ui

import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.repository.SheetMusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** 악보 저장소 가짜. 목록은 테스트가 직접 바꾸고, 읽기·삭제 결과도 테스트가 정한다. */
class FakeSheetMusicRepository : SheetMusicRepository {
    val scores = MutableStateFlow<List<SheetMusic>>(emptyList())
    val musicXmlById = mutableMapOf<String, Result<String>>()
    var deleteResult: Result<Unit> = Result.success(Unit)
    val deleted = mutableListOf<SheetMusic>()
    var getScoreCalls = 0

    override fun getAllScores(): Flow<List<SheetMusic>> = scores

    override suspend fun getScore(id: String): SheetMusic? {
        getScoreCalls++
        return scores.value.firstOrNull { it.id == id }
    }

    override suspend fun readMusicXml(score: SheetMusic): Result<String> =
        musicXmlById[score.id] ?: Result.failure(IllegalStateException("no file: ${score.id}"))

    override suspend fun deleteScore(score: SheetMusic): Result<Unit> {
        deleted += score
        if (deleteResult.isSuccess) scores.value = scores.value - score
        return deleteResult
    }
}

fun score(id: String, title: String = "악보 $id") = SheetMusic(
    id = id,
    title = title,
    recordId = 1,
    createdAt = 0L,
    bpm = 120,
    beatsPerBar = 4,
    keyName = "C major",
    noteCount = 8,
    musicXmlPath = "/scores/$id.musicxml",
    midiPath = "/scores/$id.mid",
)
