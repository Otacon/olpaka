package org.cyanotic.olpaka.feature.chat

import dev.mokkery.*
import dev.mokkery.answering.returns
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.cyanotic.olpaka.core.Analytics
import org.cyanotic.olpaka.core.DownloadState
import org.cyanotic.olpaka.core.ModelDownloadState
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.repository.ChatRepository
import org.cyanotic.olpaka.repository.ModelsRepository
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val chatRepository = mock<ChatRepository>()
    private val modelsRepository = mock<ModelsRepository>()
    private val modelDownloadState = mock<ModelDownloadState>()
    private val analytics = mock<Analytics>(mode = MockMode.autoUnit)
    private val preferences = mock<Preferences>()

    @Test
    fun given_noModels_when_openingChat_then_contentWithNoModels() = runTest {
        // GIVEN
        val viewModel = chatViewModel(testScheduler)
        everySuspend { modelsRepository.getModels() } returns Result.success(emptyList())
        everySuspend { modelDownloadState.currentDownloadState } returns MutableStateFlow(DownloadState.INACTIVE)
        every { preferences.lastUsedModel } returns null

        // WHEN
        viewModel.onCreate()
        advanceUntilIdle()

        // THEN
        val loadedState = ChatState.Error(
            title = "Title",
            message = "Message",
            showTryAgain = false,
        )
        assertEquals(loadedState, viewModel.state.value)
    }

    @Test
    fun given_someModels_when_openingChat_then_contentWithNoModels() = runTest {
        // GIVEN
        val viewModel = chatViewModel(testScheduler)
        everySuspend { modelsRepository.getModels() } returns Result.success(listOf(cachedModel))
        everySuspend { modelDownloadState.currentDownloadState } returns MutableStateFlow(DownloadState.INACTIVE)
        every { preferences.lastUsedModel } returns null

        // WHEN
        viewModel.onCreate()
        advanceUntilIdle()

        // THEN
        val loadedState = ChatState.Content(
            messages = emptyList(),
            models = listOf(cachedModelUi),
            selectedModel = cachedModelUi,
            controlsEnabled = true,
        )
        assertEquals(loadedState, viewModel.state.value)
    }

    @Test
    fun when_openingChat_then_screenViewIsTracked() = runTest {
        val viewModel = chatViewModel(testScheduler)
        everySuspend { modelsRepository.getModels() } returns Result.success(emptyList())
        everySuspend { modelDownloadState.currentDownloadState } returns MutableStateFlow(DownloadState.INACTIVE)
        every { preferences.lastUsedModel } returns null

        viewModel.onCreate()
        advanceUntilIdle()

        verify { analytics.screenView("chat") }
    }

    private fun chatViewModel(dispatcher: TestCoroutineScheduler) = ChatViewModel(
        chatRepository = chatRepository,
        modelsRepository = modelsRepository,
        modelDownloadState = modelDownloadState,
        analytics = analytics,
        preferences = preferences,
        backgroundDispatcher = StandardTestDispatcher(dispatcher)
    )

    companion object {
        private val cachedModel = Model.Cached(id = "id", name= "", size = 1, quantization = "quantization", parameters = "parameters")
        private val cachedModelUi = ChatModelUI(key="id", name="id")
    }
}