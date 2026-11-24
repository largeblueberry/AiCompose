package com.largeblueberry.data.mapper

import com.largeblueberry.data.audio.AudioRecordEntity
import com.largeblueberry.domain.model.LibraryModel

object LibraryMapper {

    fun toDomain(entity: AudioRecordEntity): LibraryModel = LibraryModel(
        id = entity.id,
        filename = entity.filename,
        filePath = entity.filePath,
        fileSize = entity.fileSize,
        duration = entity.duration,
        createdAt = entity.createdAt,
        bpm = entity.bpm,
        beatsPerBar = entity.beatsPerBar,
        sampleRate = entity.sampleRate,
    )

    fun toEntity(domain: LibraryModel): AudioRecordEntity = AudioRecordEntity(
        id = domain.id,
        filename = domain.filename,
        filePath = domain.filePath,
        fileSize = domain.fileSize,
        duration = domain.duration,
        createdAt = domain.createdAt,
        bpm = domain.bpm,
        beatsPerBar = domain.beatsPerBar,
        sampleRate = domain.sampleRate,
    )
}
