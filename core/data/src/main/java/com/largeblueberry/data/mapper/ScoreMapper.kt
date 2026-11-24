package com.largeblueberry.data.mapper

import com.largeblueberry.data.score.ScoreEntity
import com.largeblueberry.domain.model.SheetMusic

object ScoreMapper {
    fun toDomain(e: ScoreEntity) = SheetMusic(
        id = e.id,
        title = e.title,
        recordId = e.recordId,
        createdAt = e.createdAt,
        bpm = e.bpm,
        beatsPerBar = e.beatsPerBar,
        keyName = e.keyName,
        noteCount = e.noteCount,
        musicXmlPath = e.musicXmlPath,
        midiPath = e.midiPath,
    )

    fun toEntity(s: SheetMusic) = ScoreEntity(
        id = s.id,
        title = s.title,
        recordId = s.recordId,
        createdAt = s.createdAt,
        bpm = s.bpm,
        beatsPerBar = s.beatsPerBar,
        keyName = s.keyName,
        noteCount = s.noteCount,
        musicXmlPath = s.musicXmlPath,
        midiPath = s.midiPath,
    )
}
