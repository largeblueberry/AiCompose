package com.largeblueberry.data.mapper

import com.largeblueberry.data.audio.AudioRecordEntity
import com.largeblueberry.domain.model.RecordModel

object RecordMapper {
    fun toDomain(entity: AudioRecordEntity): RecordModel = RecordModel(
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

    fun toEntity(model: RecordModel): AudioRecordEntity = AudioRecordEntity(
        id = model.id,
        filename = model.filename,
        filePath = model.filePath,
        fileSize = model.fileSize,
        duration = model.duration,
        createdAt = model.createdAt,
        bpm = model.bpm,
        beatsPerBar = model.beatsPerBar,
        sampleRate = model.sampleRate,
    )
}
