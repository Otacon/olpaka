package org.cyanotic.olpaka.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.cyanotic.olpaka.core.*
import org.cyanotic.olpaka.core.DownloadState.*
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.repository.ChatMessage
import org.cyanotic.olpaka.repository.ChatRepository
import org.cyanotic.olpaka.repository.ModelsRepository

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val modelsRepository: ModelsRepository,
    private val modelDownloadState: ModelDownloadState,
    private val analytics: Analytics,
    private val preferences: Preferences,
    private val backgroundDispatcher: CoroutineDispatcher,
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

    fun onCreate() = viewModelScope.launch(backgroundDispatcher) {
        analytics.screenView("chat")
        launch {
            modelDownloadState.currentDownloadState.collect {
                when (it) {
                    DOWNLOADING -> Unit
                    COMPLETED,
                    INACTIVE -> refreshModels()
                }
            }
        }
        refreshModels()
        delay(50)
        _events.emit(ChatEvent.FocusOnTextInput)
    }

    fun onRefresh() = inBackground {
        refreshModels()
    }

    private suspend fun refreshModels() = stateMutex.withLock {
        if (models.isEmpty() && messages.isEmpty()) {
            _state.value = ChatState.Loading
        } else {
            _state.value = ChatState.Content(
                models = models.toChatModelUI(),
                messages = (messages + newMessage).filterNotNull().toMessageUI(),
                selectedModel = selectedModel?.toChatModelUI(),
                controlsEnabled = false
            )
        }
        val result = modelsRepository.getModels()
        if (result.isSuccess) {
            val newModels = result.getOrThrow().filterIsInstance<Model.Cached>()
            this.models = newModels
            if (newModels.isEmpty()) {
                selectedModel = null
                _state.value = ChatState.Error(
                    title = "Title",
                    message = "Message",
                    showTryAgain = false,
                )
            } else {
                if (selectedModel == null || !newModels.contains(selectedModel)) {
                    selectedModel = newModels.firstOrNull { it.id == preferences.lastUsedModel } ?: newModels.first()
                }
                _state.value = ChatState.Content(
                    messages = (messages + newMessage).filterNotNull().toMessageUI(),
                    models = newModels.toChatModelUI(),
                    selectedModel = selectedModel?.toChatModelUI(),
                    controlsEnabled = true
                )
            }
        } else {
            _state.value = ChatState.Error(
                title = "Error",
                message = "Error",
                showTryAgain = true
            )
        }
    }

    fun onSubmit(message: String) = inBackground {
        val currentSelectedModel = selectedModel ?: return@inBackground
        val userMessage = ChatMessage.User(message)
        var assistantMessage = ChatMessage.Assistant(message = "", isGenerating = true)
        chatRepository.sendChatMessage(model = currentSelectedModel.id, message = message, history = messages)
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
            .onCompletion { throwable ->
                stateMutex.withLock {
                    val newMessages = if (throwable != null) {
                        messages + userMessage
                    } else {
                        messages + userMessage + assistantMessage
                    }
                    messages = newMessages
                    _state.value = ChatState.Content(
                        messages = newMessages.toMessageUI(),
                        models = models.toChatModelUI(),
                        selectedModel = selectedModel?.toChatModelUI(),
                        controlsEnabled = true
                    )
                }
                delay(50)
                _events.emit(ChatEvent.FocusOnTextInput)
            }
            .collect { chunk ->
                assistantMessage = assistantMessage.copy(
                    message = assistantMessage.message + chunk.message.content,
                    isGenerating = chunk.done?.not() ?: false
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

    fun onModelChanged(model: ChatModelUI) = inBackground {
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
}

private fun Model.Cached.toChatModelUI() = ChatModelUI(this.id, this.id)

private fun List<Model.Cached>.toChatModelUI() = map { it.toChatModelUI() }

private fun List<ChatMessage>.toMessageUI() = map {
    when (it) {
        is ChatMessage.Assistant -> ChatMessageUI.Assistant(
            text = it.message,
            isGenerating = it.isGenerating
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
        val isGenerating: Boolean
    ) : ChatMessageUI
}

