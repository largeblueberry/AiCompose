package com.largeblueberry.feature_sheetmusic.ui.history

import com.largeblueberry.domain.usecase.sheetmusic.DeleteScoreUseCase
import com.largeblueberry.domain.usecase.sheetmusic.GetScoresUseCase
import com.largeblueberry.feature_sheetmusic.ui.FakeSheetMusicRepository
import com.largeblueberry.feature_sheetmusic.ui.score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** 악보 목록 ViewModel. 목록은 저장소 Flow를 그대로 따르고(SSOT), 삭제 결과도 그 Flow로만 반영된다. */
@OptIn(ExperimentalCoroutinesApi::class)
class ScoreHistoryViewModelTest {

    private val repository = FakeSheetMusicRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(): ScoreHistoryViewModel {
        val vm = ScoreHistoryViewModel(
            getScoresUseCase = GetScoresUseCase(repository),
            deleteScoreUseCase = DeleteScoreUseCase(repository),
        )
        // scores는 구독이 있어야 갱신된다(WhileSubscribed)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.scores.collect {} }
        return vm
    }

    @Test
    fun `저장소 목록이 바뀌면 그대로 따라간다`() = runTest {
        val vm = viewModel()
        assertEquals(emptyList<Any>(), vm.scores.value)

        val a = score("a")
        val b = score("b")
        repository.scores.value = listOf(a, b)

        assertEquals(listOf(a, b), vm.scores.value)
    }

    @Test
    fun `삭제하면 저장소에 위임하고 목록에서 빠진다`() = runTest {
        val a = score("a")
        val b = score("b")
        repository.scores.value = listOf(a, b)
        val vm = viewModel()

        vm.deleteScore(a)

        assertEquals(listOf(a), repository.deleted)
        assertEquals(listOf(b), vm.scores.value)
    }

    @Test
    fun `삭제가 실패해도 죽지 않고 목록은 그대로다`() = runTest {
        val a = score("a")
        repository.scores.value = listOf(a)
        repository.deleteResult = Result.failure(java.io.IOException("locked"))
        val vm = viewModel()

        vm.deleteScore(a)

        assertEquals(listOf(a), repository.deleted)
        assertEquals(listOf(a), vm.scores.value)
    }
}
