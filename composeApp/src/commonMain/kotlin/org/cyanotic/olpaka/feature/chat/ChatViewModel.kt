package org.cyanotic.olpaka.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import olpaka.composeapp.generated.resources.*
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.error_missing_ollama_message
import olpaka.composeapp.generated.resources.error_missing_ollama_title
import org.cyanotic.olpaka.core.Analytics
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.StringResources
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.repository.ChatMessage
import org.cyanotic.olpaka.repository.ChatRepository
import org.cyanotic.olpaka.repository.ModelsRepository

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val modelsRepository: ModelsRepository,
    private val analytics: Analytics,
    private val preferences: Preferences,
    private val backgroundDispatcher: CoroutineDispatcher,
    private val strings: StringResources,
) : ViewModel() {

    private val _state = MutableStateFlow<ChatState>(ChatState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChatEvent>()
    val event = _events.asSharedFlow()

    private val stateMutex = Mutex()

    private var models = listOf<Model.Cached>()
    private var messages = listOf<ChatMessage>()
    private var newMessage: ChatMessage.Assistant? = null
    private var selectedModel: Model.Cached? = null

    init {
        viewModelScope.launch(backgroundDispatcher) {
            modelsRepository.models
                .map { it.filterIsInstance<Model.Cached>() }
                .collect { cachedModels ->
                    stateMutex.withLock {
                        models = cachedModels
                        calculateState(cachedModels)
                    }
                }
        }
    }

    fun onCreate() = viewModelScope.launch(backgroundDispatcher) {
        analytics.screenView("chat")
        refreshModels()
        focusOnTextInput()
    }

    fun onRefresh() = viewModelScope.launch(backgroundDispatcher) {
        _state.value = ChatState.Loading
        refreshModels()
    }

    private suspend fun refreshModels() {
        val result = modelsRepository.refreshModels()
        if(result.isFailure) {
            _state.value = ChatState.Error(
                title = strings.get(Res.string.error_missing_ollama_title),
                message = strings.get(Res.string.error_missing_ollama_message),
                showTryAgain = true
            )
        } else {
            val models = result.getOrNull()!!.filterIsInstance<Model.Cached>()
            calculateState(models)
        }
    }

    private suspend fun calculateState(models: List<Model.Cached>) {
        if (models.isEmpty()) {
            selectedModel = null
            _state.value = ChatState.Error(
                title = strings.get(Res.string.chat_missing_model_error_title),
                message = strings.get(Res.string.chat_missing_model_error_message),
                showTryAgain = true,
            )
            return
        }

        if (selectedModel == null || !models.contains(selectedModel)) {
            selectedModel = models.firstOrNull { it.id == preferences.lastUsedModel }
                ?: models.first()
        }
        _state.value = ChatState.Content(
            messages = (messages + newMessage).filterNotNull().toMessageUI(),
            models = models.toChatModelUI(),
            selectedModel = selectedModel?.toChatModelUI(),
            controlsEnabled = true
        )
    }

    fun onSubmit(message: String) = viewModelScope.launch(backgroundDispatcher) {
        val currentSelectedModel = stateMutex.withLock { selectedModel } ?: return@launch
        val userMessage = ChatMessage.User(message)
        var assistantMessage = ChatMessage.Assistant(
            message = "",
            isError = false,
            isGenerating = true,
        )
        var throwable: Throwable? = null
        chatRepository.sendChatMessage(
            model = currentSelectedModel.id,
            message = message,
            history = messages
        )
            .catch { e -> throwable = e }
            .onStart {
                analytics.event("send_message", mapOf("model" to currentSelectedModel.id))
                stateMutex.withLock {
                    preferences.lastUsedModel = currentSelectedModel.id
                    val newMessages = messages + userMessage + assistantMessage
                    _events.emit(ChatEvent.ClearTextInput)
                    _state.value = ChatState.Content(
                        messages = newMessages.toMessageUI(),
                        models = models.toChatModelUI(),
                        selectedModel = selectedModel?.toChatModelUI(),
                        controlsEnabled = false
                    )
                }
            }
            .onCompletion {
                stateMutex.withLock {
                    messages = messages + userMessage + assistantMessage.copy(
                        isError = throwable != null,
                        isGenerating = false
                    )
                    _state.value = ChatState.Content(
                        messages = messages.toMessageUI(),
                        models = models.toChatModelUI(),
                        selectedModel = selectedModel?.toChatModelUI(),
                        controlsEnabled = true
                    )
                }
                focusOnTextInput()
            }
            .collect { chunk ->
                assistantMessage = assistantMessage.copy(
                    message = assistantMessage.message + chunk.message.content,
                    isGenerating = chunk.done?.not() ?: true
                )
                stateMutex.withLock {
                    val newMessages = messages + userMessage + assistantMessage
                    _state.value = ChatState.Content(
                        messages = newMessages.toMessageUI(),
                        models = models.toChatModelUI(),
                        selectedModel = selectedModel?.toChatModelUI(),
                        controlsEnabled = false
                    )
                }

            }

    }

    fun onModelChanged(model: ChatModelUI) = viewModelScope.launch(backgroundDispatcher) {
        analytics.event("model_changed", mapOf("model" to model.key))
        stateMutex.withLock {
            selectedModel = models.firstOrNull { it.id == model.key }
            _state.value = ChatState.Content(
                messages = messages.toMessageUI(),
                models = models.toChatModelUI(),
                selectedModel = selectedModel?.toChatModelUI(),
                controlsEnabled = true
            )
        }
    }

    private fun focusOnTextInput() = viewModelScope.launch {
        delay(50)
        _events.emit(ChatEvent.FocusOnTextInput)
    }
}

private fun Model.Cached.toChatModelUI() = ChatModelUI(this.id, this.id)

private fun List<Model.Cached>.toChatModelUI() = map { it.toChatModelUI() }

private fun List<ChatMessage>.toMessageUI() = map {
    when (it) {
        is ChatMessage.Assistant -> ChatMessageUI.Assistant(
            text = it.message,
            isGenerating = it.isGenerating,
            isError = it.isError
        )

        is ChatMessage.User -> ChatMessageUI.User(
            text = it.message
        )
    }

}


sealed interface ChatEvent {
    data object ClearTextInput : ChatEvent
    data object FocusOnTextInput : ChatEvent
}

sealed interface ChatState {
    data class Content(
        val messages: List<ChatMessageUI> = emptyList(),
        val models: List<ChatModelUI> = emptyList(),
        val selectedModel: ChatModelUI? = null,
        val controlsEnabled: Boolean,
    ) : ChatState

    data class Error(
        val title: String,
        val message: String,
        val showTryAgain: Boolean,
    ) : ChatState

    data object Loading : ChatState
}

data class ChatModelUI(
    val key: String,
    val name: String,
)

sealed interface ChatMessageUI {

    data class User(
        val text: String,
    ) : ChatMessageUI

    data class Assistant(
        val text: String,
        val isGenerating: Boolean,
        val isError: Boolean,
    ) : ChatMessageUI
}

