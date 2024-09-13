package org.cyanotic.olpaka.feature.chat

import kotlinx.coroutines.flow.*
import org.cyanotic.olpaka.core.OlpakaViewModel
import org.cyanotic.olpaka.repository.ChatMessage
import org.cyanotic.olpaka.repository.ChatRepository
import org.cyanotic.olpaka.repository.GetModelsResult
import org.cyanotic.olpaka.repository.ModelsRepository

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val modelsRepository: ModelsRepository,
) : OlpakaViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChatEvent>()
    val event = _events.asSharedFlow()

    override fun onCreate() = inBackground {
        _state.value = _state.value.copy(isLoading = true)
        val models = when (val result = modelsRepository.getModels()) {
            GetModelsResult.Failure -> emptyList()
            is GetModelsResult.Success -> result.models.map { ChatModelUI(it.tag, it.name) }
        }
        _state.value = _state.value.copy(
            models = models,
            selectedModel = models.firstOrNull(),
            isLoading = false
        )
    }

    fun onSubmit(message: String) = inBackground {
        val selectedModel = _state.value.selectedModel ?: return@inBackground
        var assistantMessage = ChatMessageUI.AssistantMessage("", true)
        val history = _state.value.messages.map {
            when (it) {
                is ChatMessageUI.AssistantMessage -> ChatMessage.Assistant(it.text)
                is ChatMessageUI.OwnMessage -> ChatMessage.User(it.text)
            }
        }
        chatRepository.sendChatMessage(model = selectedModel.key, message = message, history = history)
            .onStart {
                val newMessages = _state.value.messages +
                        ChatMessageUI.OwnMessage(message) +
                        assistantMessage
                _state.value = _state.value.copy(messages = newMessages, isLoading = true)
            }
            .onCompletion {
                _state.value = _state.value.copy(isLoading = false)
            }
            .collect { chunk ->
                assistantMessage = assistantMessage.copy(text = assistantMessage.text + chunk.message.content)
                val newMessages = _state.value.messages.subList(0, _state.value.messages.size - 1) + assistantMessage
                _state.value = _state.value.copy(messages = newMessages)
            }

    }

    fun onModelChanged(model: ChatModelUI) {
        _state.getAndUpdate { current ->
            current.copy(selectedModel = current.models.firstOrNull { it.key == model.key })
        }
    }
}


sealed interface ChatEvent

data class ChatState(
    val messages: List<ChatMessageUI> = emptyList(),
    val models: List<ChatModelUI> = emptyList(),
    val selectedModel: ChatModelUI? = null,
    val isLoading: Boolean = false,
)

data class ChatModelUI(
    val key: String,
    val name: String,
)

sealed interface ChatMessageUI {

    data class OwnMessage(
        val text: String,
    ) : ChatMessageUI

    data class AssistantMessage(
        val text: String,
        val isGenerating: Boolean
    ) : ChatMessageUI
}

