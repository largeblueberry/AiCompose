package com.largeblueberry.feature_sheetmusic.ui

import androidx.lifecycle.SavedStateHandle
import com.largeblueberry.domain.usecase.sheetmusic.GetSheetMusicDetailUseCase
import com.largeblueberry.feature_sheetmusic.ui.state.SheetMusicUiState
import com.largeblueberry.navigation.AppRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * 악보 상세 ViewModel. id는 내비게이션 인자(SavedStateHandle)로만 들어오고, 화면 호출 없이 스스로 불러온다.
 * 가짜 저장소가 즉시 돌려주고 Main이 Unconfined라 init의 불러오기가 생성 직후 끝난다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SheetMusicViewModelTest {

    private val repository = FakeSheetMusicRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(id: String?) = SheetMusicViewModel(
        savedStateHandle = SavedStateHandle(
            if (id == null) emptyMap() else mapOf(AppRoutes.SheetMusicScreen.SCORE_ID_ARG to id)
        ),
        getSheetMusicDetailUseCase = GetSheetMusicDetailUseCase(repository),
    )

    @Test
    fun `id 인자가 없으면 저장소를 부르지 않고 NotFound`() {
        val vm = viewModel(id = null)

        assertEquals(SheetMusicUiState.NotFound, vm.uiState.value)
        assertEquals(0, repository.getScoreCalls)
    }

    @Test
    fun `악보와 파일이 있으면 Success에 MusicXML을 담는다`() {
        val s = score("a")
        repository.scores.value = listOf(s)
        repository.musicXmlById["a"] = Result.success("<score-partwise/>")

        val vm = viewModel("a")

        assertEquals(SheetMusicUiState.Success(s, "<score-partwise/>"), vm.uiState.value)
    }

    @Test
    fun `지워졌거나 없는 id면 NotFound`() {
        repository.scores.value = listOf(score("a"))

        val vm = viewModel("gone")

        assertEquals(SheetMusicUiState.NotFound, vm.uiState.value)
    }

    @Test
    fun `DB 행은 있는데 파일을 못 읽으면 Error`() {
        repository.scores.value = listOf(score("a"))
        repository.musicXmlById["a"] = Result.failure(java.io.IOException("missing"))

        val vm = viewModel("a")

        assertEquals(SheetMusicUiState.Error, vm.uiState.value)
    }

    @Test
    fun `Error 뒤 retry로 다시 불러와 Success가 된다`() {
        val s = score("a")
        repository.scores.value = listOf(s)
        repository.musicXmlById["a"] = Result.failure(java.io.IOException("busy"))
        val vm = viewModel("a")
        assertEquals(SheetMusicUiState.Error, vm.uiState.value)

        repository.musicXmlById["a"] = Result.success("<x/>")
        vm.retry()

        assertEquals(SheetMusicUiState.Success(s, "<x/>"), vm.uiState.value)
        assertEquals(2, repository.getScoreCalls)
    }
}
