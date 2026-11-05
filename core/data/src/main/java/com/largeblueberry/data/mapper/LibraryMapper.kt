package com.largeblueberry.data.mapper

import com.largeblueberry.data.audio.AudioRecordEntity
import com.largeblueberry.domain.model.LibraryModel

object LibraryMapper {

    fun toDomain(entity: AudioRecordEntity): LibraryModel {
        return LibraryModel(
            id = entity.id,
            filename = entity.filename,
            filePath = entity.filePath,
            fileSize = entity.fileSize,
            duration = entity.duration,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: LibraryModel): AudioRecordEntity {
        return AudioRecordEntity(
            id = domain.id,
            filename = domain.filename,
            filePath = domain.filePath,
            fileSize = domain.fileSize,
            duration = domain.duration,
            createdAt = domain.createdAt
        )
    }
}
