package org.cyanotic.olpaka.feature.chat

import app.cash.turbine.test
import dev.mokkery.*
import dev.mokkery.answering.returns
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import olpaka.composeapp.generated.resources.*
import org.cyanotic.olpaka.core.*
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.network.ChatMessageDTO
import org.cyanotic.olpaka.network.ChatResponseDTO
import org.cyanotic.olpaka.network.Role
import org.cyanotic.olpaka.repository.ChatRepository
import org.cyanotic.olpaka.repository.ModelsRepository
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val chatRepository = mock<ChatRepository>()
    private val modelsRepository = mock<ModelsRepository>()
    private val modelDownloadState = mock<ModelDownloadState> {
        everySuspend { currentDownloadState } returns MutableStateFlow(DownloadState.INACTIVE)
    }
    private val analytics = mock<Analytics>(mode = MockMode.autoUnit)
    private val preferences = mock<Preferences>(mode = MockMode.autoUnit)
    private val strings = mock<StringResources> {
        everySuspend { get(Res.string.models_error_no_models_title) } returns "no models title"
        everySuspend { get(Res.string.models_error_no_models_message) } returns "no models message"
        everySuspend { get(Res.string.error_missing_ollama_title) } returns "missing ollama title"
        everySuspend { get(Res.string.error_missing_ollama_message) } returns "missing ollama message"
    }

    @Test
    fun given_noModels_when_openingChat_then_contentWithNoModels() = runTestOn { viewModel ->
        // GIVEN
        everySuspend { modelsRepository.getModels() } returns Result.success(emptyList())
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
    fun given_someModels_when_openingChat_then_contentWithFirstModelSelected() = runTestOn { viewModel ->
        // GIVEN
        everySuspend { modelsRepository.getModels() } returns Result.success(listOf(cachedModel1, cachedModel2))
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
    fun given_someModels_when_openingChat_then_preferenceModelSelected() = runTestOn { viewModel ->
        // GIVEN
        everySuspend { modelsRepository.getModels() } returns Result.success(listOf(cachedModel1, cachedModel2))
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
    fun given_ollamaNotAvailable_when_openingChat_then_errorIsShown() = runTestOn { viewModel ->
        // GIVEN
        everySuspend { modelsRepository.getModels() } returns Result.failure(RuntimeException("Error"))
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
    fun when_openingChat_then_screenViewIsTracked() = runTestOn { viewModel ->
        // GIVEN
        everySuspend { modelsRepository.getModels() } returns Result.success(emptyList())
        every { preferences.lastUsedModel } returns null

        // WHEN
        viewModel.onCreate()
        advanceUntilIdle()

        // THEN
        verify { analytics.screenView("chat") }
    }

    @Test
    fun when_openingChat_then_textInputIsFocussed() = runTestOn { viewModel ->
        // GIVEN
        everySuspend { modelsRepository.getModels() } returns Result.success(emptyList())

        every { preferences.lastUsedModel } returns null

        // WHEN
        viewModel.event.test {
            viewModel.onCreate()
            advanceUntilIdle()

            // THEN
            val event = awaitItem()
            assertEquals(ChatEvent.FocusOnTextInput, event)
        }
    }

    @Test
    fun when_userSelectsAModel_then_modelIsSelected() = runTestOn { viewModel ->
        // GIVEN
        everySuspend { modelsRepository.getModels() } returns Result.success(listOf(cachedModel1, cachedModel2))
        every { preferences.lastUsedModel } returns cachedModel1.id

        // WHEN
        viewModel.onCreate()
        viewModel.onModelChanged(cachedModel2Ui)
        advanceUntilIdle()

        // THEN
        val expectedState = ChatState.Content(
            messages = emptyList(),
            models = listOf(cachedModel1Ui, cachedModel2Ui),
            selectedModel = cachedModel2Ui,
            controlsEnabled = true
        )

        assertEquals(expectedState, viewModel.state.value)
    }

    @Test
    fun given_generationIsSuccessful_when_generatingMessage_then_progressAndSuccessShouldBeShown() = runTestOn { viewModel ->
        // GIVEN
        val query = "why is the sky blue?"
        everySuspend { modelsRepository.getModels() } returns Result.success(listOf(cachedModel1))
        every { preferences.lastUsedModel } returns cachedModel1.id
        everySuspend {
            chatRepository.sendChatMessage(
                model = cachedModel1.id,
                message = query,
                history = emptyList()
            )
        } returns flowOf(
            ChatResponseDTO(
                model = cachedModel1.id,
                message = ChatMessageDTO(
                    role = Role.ASSISTANT,
                    content = ""
                ),
                done = null,
            ),
            ChatResponseDTO(
                model = cachedModel1.id,
                message = ChatMessageDTO(
                    role = Role.ASSISTANT,
                    content = "because of "
                ),
                done = false,
            ),
            ChatResponseDTO(
                model = cachedModel1.id,
                message = ChatMessageDTO(
                    role = Role.ASSISTANT,
                    content = "light."
                ),
                done = true,
            ),
        )

        // WHEN
        viewModel.onCreate()
        advanceUntilIdle()
        viewModel.state.test {
            viewModel.onSubmit(query)
            advanceUntilIdle()
            val userMessage = ChatMessageUI.User(query)
            var assistantMessage = ChatMessageUI.Assistant("", true)
            var expectedState = ChatState.Content(
                messages = emptyList(),
                models = listOf(cachedModel1Ui),
                selectedModel = cachedModel1Ui,
                controlsEnabled = true
            )

            assertEquals(expectedState, awaitItem())

            expectedState = expectedState.copy(messages = listOf(userMessage, assistantMessage), controlsEnabled = false)
            assertEquals(expectedState, awaitItem())

            assistantMessage = assistantMessage.copy(text = "because of ")
            expectedState = expectedState.copy(messages = listOf(userMessage, assistantMessage), controlsEnabled = false)
            assertEquals(expectedState, awaitItem())

            assistantMessage = assistantMessage.copy(text = "because of light.", isGenerating = false)
            expectedState = expectedState.copy(messages = listOf(userMessage, assistantMessage), controlsEnabled = false)
            assertEquals(expectedState, awaitItem())

            expectedState = expectedState.copy(messages = listOf(userMessage, assistantMessage), controlsEnabled = true)
            assertEquals(expectedState, awaitItem())
        }

    }

    private fun runTestOn(body: suspend TestScope.(viewModel: ChatViewModel) -> Unit) = runTest {
        val viewModel = ChatViewModel(
            chatRepository = chatRepository,
            modelsRepository = modelsRepository,
            modelDownloadState = modelDownloadState,
            analytics = analytics,
            preferences = preferences,
            backgroundDispatcher = StandardTestDispatcher(testScheduler),
            strings = strings
        )
        body(viewModel)
    }

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