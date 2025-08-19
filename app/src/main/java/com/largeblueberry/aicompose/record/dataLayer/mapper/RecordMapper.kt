package com.largeblueberry.aicompose.record.dataLayer.mapper

import com.largeblueberry.aicompose.data.record.local.AudioRecordEntity
import com.largeblueberry.aicompose.record.domain.model.RecordModel

object RecordMapper {
    fun toDomain(audioRecordEntity: AudioRecordEntity): RecordModel {
        return RecordModel(
            id = audioRecordEntity.id,
            filename = audioRecordEntity.filename,
            filePath = audioRecordEntity.filePath,
            fileSize = audioRecordEntity.fileSize,
            duration = audioRecordEntity.duration,
            createdAt = audioRecordEntity.createdAt
        )
    }

    fun toEntity(recordModel: RecordModel): AudioRecordEntity {
        return AudioRecordEntity(
            id = recordModel.id,
            filename = recordModel.filename,
            filePath = recordModel.filePath,
            fileSize = recordModel.fileSize,
            duration = recordModel.duration,
            createdAt = recordModel.createdAt
        )
    }
}