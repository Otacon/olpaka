package org.cyanotic.olpaka.repository

import kotlinx.coroutines.flow.flow
import org.cyanotic.olpaka.network.*

class ChatRepository(
    private val client: OllamaRestClient,
) {

    fun sendChatMessage(
        model: String,
        message: String,
        history: List<ChatMessage> = emptyList()
    ) = flow {
        val messages = history.map {
            when (it) {
                is ChatMessage.Assistant -> ChatMessageDTO(Role.ASSISTANT, it.message)
                is ChatMessage.User -> ChatMessageDTO(Role.USER, it.message)
            }
        } + ChatMessageDTO(Role.USER, message)
        val request = ChatMessageRequestDTO(
            model = model,
            messages = messages,
            options = ChatOptionsDTO(
                temperature = 0.8f,
                numContext = 4096
            )
        )
        client.sendChatMessage(request).collect(this)
    }
}


sealed interface ChatMessage {
    data class User(
        val message: String
    ) : ChatMessage

    data class Assistant(
        val message: String
    ) : ChatMessage
}
