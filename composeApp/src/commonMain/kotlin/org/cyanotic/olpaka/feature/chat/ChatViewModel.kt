package org.cyanotic.olpaka.feature.chat

import kotlinx.coroutines.flow.*
import org.cyanotic.olpaka.core.OlpakaViewModel
import org.cyanotic.olpaka.repository.GenerateRepository
import org.cyanotic.olpaka.repository.GetModelsResult
import org.cyanotic.olpaka.repository.ModelsRepository

class ChatViewModel(
    private val generateRepository: GenerateRepository,
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
        _state.value = _state.value.copy(models = models, isLoading = false)
    }

    fun sendMessage(message: String) = inBackground {
        val selectedModel = _state.value.selectedModel ?: return@inBackground
        var assistantMessage = ChatMessageUI.AssistantMessage("", true)
        generateRepository.generate(message, selectedModel.key)
            .onStart {
                val newMessages = _state.value.messages +
                        ChatMessageUI.OwnMessage(message) +
                        assistantMessage
                _state.value = _state.value.copy(messages = newMessages, isLoading = true)
            }
            .onCompletion {
                _state.value = _state.value.copy(isLoading = false)
            }
            .collect {
                assistantMessage = assistantMessage.copy(text = assistantMessage.text + it.response)
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

