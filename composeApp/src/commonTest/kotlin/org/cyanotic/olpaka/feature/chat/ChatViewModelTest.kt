package org.cyanotic.olpaka.feature.chat

import dev.mokkery.*
import dev.mokkery.answering.returns
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import olpaka.composeapp.generated.resources.*
import org.cyanotic.olpaka.core.*
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
    private val strings = mock<StringResources> {
        everySuspend { get(Res.string.models_error_no_models_title) } returns "no models title"
        everySuspend { get(Res.string.models_error_no_models_message) } returns "no models message"
        everySuspend { get(Res.string.error_missing_ollama_title) } returns "missing ollama title"
        everySuspend { get(Res.string.error_missing_ollama_message) } returns "missing ollama message"
    }

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
            title = "no models title",
            message = "no models message",
            showTryAgain = true,
        )
        assertEquals(loadedState, viewModel.state.value)
    }

    @Test
    fun given_someModels_when_openingChat_then_contentWithFirstModelSelected() = runTest {
        // GIVEN
        val viewModel = chatViewModel(testScheduler)
        everySuspend { modelsRepository.getModels() } returns Result.success(listOf(cachedModel1, cachedModel2))
        everySuspend { modelDownloadState.currentDownloadState } returns MutableStateFlow(DownloadState.INACTIVE)
        every { preferences.lastUsedModel } returns null

        // WHEN
        viewModel.onCreate()
        advanceUntilIdle()

        // THEN
        val loadedState = ChatState.Content(
            messages = emptyList(),
            models = listOf(cachedModel1Ui, cachedModel2Ui),
            selectedModel = cachedModel1Ui,
            controlsEnabled = true,
        )
        assertEquals(loadedState, viewModel.state.value)
    }

    @Test
    fun given_someModels_when_openingChat_then_preferenceModelSelected() = runTest {
        // GIVEN
        val viewModel = chatViewModel(testScheduler)
        everySuspend { modelsRepository.getModels() } returns Result.success(listOf(cachedModel1, cachedModel2))
        everySuspend { modelDownloadState.currentDownloadState } returns MutableStateFlow(DownloadState.INACTIVE)
        every { preferences.lastUsedModel } returns cachedModel2.id

        // WHEN
        viewModel.onCreate()
        advanceUntilIdle()

        // THEN
        val loadedState = ChatState.Content(
            messages = emptyList(),
            models = listOf(cachedModel1Ui, cachedModel2Ui),
            selectedModel = cachedModel2Ui,
            controlsEnabled = true,
        )
        assertEquals(loadedState, viewModel.state.value)
    }

    @Test
    fun given_ollamaNotAvailable_when_openingChat_then_errorIsShown() = runTest {
        // GIVEN
        val viewModel = chatViewModel(testScheduler)
        everySuspend { modelsRepository.getModels() } returns Result.failure(RuntimeException("Error"))
        everySuspend { modelDownloadState.currentDownloadState } returns MutableStateFlow(DownloadState.INACTIVE)
        every { preferences.lastUsedModel } returns null

        // WHEN
        viewModel.onCreate()
        advanceUntilIdle()

        // THEN
        val loadedState = ChatState.Error(
            title = "missing ollama title",
            message = "missing ollama message",
            showTryAgain = true
        )
        assertEquals(loadedState, viewModel.state.value)
    }

    @Test
    fun when_openingChat_then_screenViewIsTracked() = runTest {
        // GIVEN
        val viewModel = chatViewModel(testScheduler)
        everySuspend { modelsRepository.getModels() } returns Result.success(emptyList())
        everySuspend { modelDownloadState.currentDownloadState } returns MutableStateFlow(DownloadState.INACTIVE)
        every { preferences.lastUsedModel } returns null

        // WHEN
        viewModel.onCreate()
        advanceUntilIdle()

        // THEN
        verify { analytics.screenView("chat") }
    }

    private fun chatViewModel(dispatcher: TestCoroutineScheduler) = ChatViewModel(
        chatRepository = chatRepository,
        modelsRepository = modelsRepository,
        modelDownloadState = modelDownloadState,
        analytics = analytics,
        preferences = preferences,
        backgroundDispatcher = StandardTestDispatcher(dispatcher),
        strings = strings
    )

    companion object {
        private val cachedModel1 = Model.Cached(
            id = "id1",
            name = "",
            size = 1,
            quantization = "quantization",
            parameters = "parameters"
        )
        private val cachedModel2 = cachedModel1.copy(
            id = "id2"
        )

        private val cachedModel1Ui = ChatModelUI(
            key = "id1",
            name = "id1"
        )
        private val cachedModel2Ui = ChatModelUI(
            key = "id2",
            name = "id2"
        )
    }
}