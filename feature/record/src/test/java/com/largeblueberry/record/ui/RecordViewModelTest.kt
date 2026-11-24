package com.largeblueberry.record.ui

import com.largeblueberry.domain.model.RecorderState
import com.largeblueberry.domain.model.Tempo
import com.largeblueberry.domain.repository.RecordingInterruptedException
import com.largeblueberry.domain.repository.RecordingTooShortException
import com.largeblueberry.domain.usecase.record.CancelRecordingUseCase
import com.largeblueberry.domain.usecase.record.ObserveRecorderStateUseCase
import com.largeblueberry.domain.usecase.record.StartRecordingUseCase
import com.largeblueberry.domain.usecase.record.StopRecordingUseCase
import com.largeblueberry.domain.usecase.setting.GetLastBpmUseCase
import com.largeblueberry.domain.usecase.setting.SetLastBpmUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 녹음 화면 ViewModel. 녹음기는 가짜라 상태를 테스트가 직접 움직인다.
 * 저장소가 main-safe하고(스레드 전환은 구현 몫) 가짜는 즉시 돌려주므로, 모든 단계가 테스트 디스패처 위에서 결정적으로 돈다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {

    private val recorder = FakeRecordRepository()
    private val prefs = FakeUserPreferencesRepository(initialBpm = 120)

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(): RecordViewModel {
        val vm = RecordViewModel(
            startRecordingUseCase = StartRecordingUseCase(recorder),
            stopRecordingUseCase = StopRecordingUseCase(recorder),
            cancelRecordingUseCase = CancelRecordingUseCase(recorder),
            observeRecorderState = ObserveRecorderStateUseCase(recorder),
            getLastBpmUseCase = GetLastBpmUseCase(prefs),
            setLastBpmUseCase = SetLastBpmUseCase(prefs),
        )
        // uiState는 구독이 있어야 갱신된다(WhileSubscribed)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    private suspend fun RecordViewModel.awaitStatus(status: RecordingState): RecordUiState =
        withTimeout(5_000) { uiState.first { it.status == status } }

    @Test
    fun lastBpmIsRestored() = runTest {
        val vm = viewModel()
        assertEquals(120, vm.uiState.value.bpm)
    }

    @Test
    fun changeBpmClampsAndPersists() = runTest {
        val vm = viewModel()
        vm.changeBpm(+500)
        assertEquals(Tempo.MAX_BPM, vm.uiState.value.bpm)
        assertEquals(Tempo.MAX_BPM, prefs.bpm.value)
    }

    @Test
    fun bpmCannotChangeWhileRecording() = runTest {
        val vm = viewModel()
        vm.startRecording()
        vm.changeBpm(+5)
        assertEquals(120, vm.uiState.value.bpm)
    }

    @Test
    fun startUsesChosenBpmAndShowsCountIn() = runTest {
        val vm = viewModel()
        vm.changeBpm(-20)
        vm.startRecording()
        assertEquals(100, recorder.startedConfigs.single().bpm)
        assertEquals(RecordingState.COUNT_IN, vm.uiState.value.status)
        assertTrue(vm.uiState.value.isActive)
    }

    @Test
    fun recordingThenStopSavesRecord() = runTest {
        val vm = viewModel()
        vm.startRecording()
        recorder.mutableState.value = RecorderState.Recording(beat = 1, elapsedMs = 2_000, clickAudible = false)
        assertEquals(RecordingState.RECORDING, vm.uiState.value.status)

        vm.stopRecording()
        val state = vm.awaitStatus(RecordingState.COMPLETED)
        assertEquals("20261123_120000.wav", state.savedFileName)
        assertEquals(false, state.stoppedAtLimit)
        assertEquals(1, recorder.savedRecords.size)
    }

    @Test
    fun savedMessageIsConsumedOnce() = runTest {
        val vm = viewModel()
        vm.startRecording()
        vm.stopRecording()
        vm.awaitStatus(RecordingState.COMPLETED)
        vm.consumeSavedMessage()
        assertEquals(null, vm.uiState.value.savedFileName)
    }

    @Test
    fun startFailureIsShown() = runTest {
        recorder.startResult = Result.failure(IllegalStateException("no mic"))
        val vm = viewModel()
        vm.startRecording()
        assertEquals(RecordingState.FAILED_START, vm.uiState.value.status)
        assertEquals(false, vm.uiState.value.isActive)
    }

    @Test
    fun stopDuringCountInIsTooShort() = runTest {
        recorder.stopResult = Result.failure(RecordingTooShortException())
        val vm = viewModel()
        vm.startRecording()
        vm.stopRecording()
        vm.awaitStatus(RecordingState.TOO_SHORT)
        assertEquals(0, recorder.savedRecords.size)
    }

    @Test
    fun stopsAutomaticallyAtTwoMinutes() = runTest {
        val vm = viewModel()
        vm.startRecording()
        recorder.mutableState.value = RecorderState.Recording(beat = 0, elapsedMs = 120_000, clickAudible = false)
        val state = vm.awaitStatus(RecordingState.COMPLETED)
        assertTrue(state.stoppedAtLimit)
        assertEquals(1, recorder.stopCalls)
    }

    @Test
    fun failureDuringRecordingIsReported() = runTest {
        recorder.stopResult = Result.failure(RecordingInterruptedException())
        val vm = viewModel()
        vm.startRecording()
        recorder.mutableState.value = RecorderState.Recording(beat = 0, elapsedMs = 1_000, clickAudible = false)
        // 녹음기가 실패를 알리면 ViewModel이 스스로 stop을 불러 원인을 받는다
        recorder.mutableState.value = RecorderState.Failed
        vm.awaitStatus(RecordingState.INTERRUPTED)
        assertEquals(1, recorder.stopCalls)
        assertEquals(0, recorder.savedRecords.size)
    }

    @Test
    fun leavingScreenWhileRecordingDiscardsIt() = runTest {
        val vm = viewModel()
        vm.startRecording()
        recorder.mutableState.value = RecorderState.Recording(beat = 0, elapsedMs = 1_000, clickAudible = false)
        vm.onScreenStopped()
        assertEquals(1, recorder.cancelCalls)
        assertEquals(RecordingState.CANCELLED, vm.uiState.value.status)
        assertEquals(0, recorder.savedRecords.size)
    }

    @Test
    fun leavingScreenWhileIdleDoesNothing() = runTest {
        val vm = viewModel()
        vm.onScreenStopped()
        assertEquals(0, recorder.cancelCalls)
        assertEquals(RecordingState.WAITING, vm.uiState.value.status)
    }
}
