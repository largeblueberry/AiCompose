package com.largeblueberry.library.dataLayer.mapper

import com.largeblueberry.local.audio.AudioRecordEntity
import com.largeblueberry.library.domainLayer.model.LibraryModel


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

/**
 * AudioRecordEntity를 받아서 LibraryAudioRecord로 변환한다.
 * 간단한 매핑에는 object가 편리함. context가 필요없으므로 충분히 싱글톤패턴으로 관리해도 좋다.
 *  도메인 모델에 id를 포함하여 객체의 정체성을 명확히 하고, 효율적인 관리와 검색 가능에 집중한다.
 */

