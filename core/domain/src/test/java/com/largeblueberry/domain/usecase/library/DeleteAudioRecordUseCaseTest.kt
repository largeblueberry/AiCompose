package com.largeblueberry.domain.usecase.library

import com.largeblueberry.domain.model.LibraryModel
import com.largeblueberry.domain.model.TranscriptionJob
import com.largeblueberry.domain.repository.LibraryRepository
import com.largeblueberry.domain.repository.TranscriptionScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** 녹음 삭제는 그 녹음의 채보 작업을 먼저 멈춘 뒤 파일·행을 지운다. */
class DeleteAudioRecordUseCaseTest {

    @get:Rule
    val tmp = TemporaryFolder()

    /** 저장소와 스케줄러가 불린 순서 */
    private val events = mutableListOf<String>()

    private val library = object : LibraryRepository {
        override fun getAllRecords(): Flow<List<LibraryModel>> = emptyFlow()
        override suspend fun deleteRecord(record: LibraryModel) { events += "delete ${record.id}" }
        override suspend fun renameRecord(record: LibraryModel) = Unit
    }

    private val scheduler = object : TranscriptionScheduler {
        override fun enqueue(recordId: Int) = Unit
        override fun cancel(recordId: Int) { events += "cancel $recordId" }
        override fun observeJobs(): Flow<Map<Int, TranscriptionJob>> = emptyFlow()
    }

    private val useCase = DeleteAudioRecordUseCase(library, scheduler)

    private fun record(id: Int) = LibraryModel(
        id = id,
        filename = "rec.wav",
        filePath = tmp.newFile("rec$id.wav").absolutePath,
        fileSize = 0,
        duration = "0:01",
        bpm = 100,
        sampleRate = 22_050,
    )

    @Test
    fun `채보 작업을 먼저 멈추고 파일과 행을 지운다`() = runBlocking {
        val r = record(7)

        val result = useCase(r)

        assertTrue(result.isSuccess)
        assertEquals(listOf("cancel 7", "delete 7"), events)
        assertFalse(java.io.File(r.filePath).exists())
    }
}
