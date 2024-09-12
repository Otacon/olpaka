package org.cyanotic.olpaka.repository

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ChatRepository(
    private val client: HttpClient,
    private val decoder: Json,
) {

    fun sendChatMessage(model: String, message: String, history: List<ChatMessage> = emptyList()) = flow {
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
        client.preparePost {
            url("http://localhost:11434/api/chat")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.execute { resp ->
            val channel = resp.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line()
                if (line != null) {
                    val chunk = decoder.decodeFromString<ChatResponseDTO>(line)
                    emit(chunk)
                }
            }
        }
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

@Serializable
data class ChatMessageRequestDTO(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<ChatMessageDTO>,
    @SerialName("options") val options: ChatOptionsDTO? = null
)

@Serializable
data class ChatResponseDTO(
    @SerialName("model") val model: String,
    @SerialName("message") val message: ChatMessageDTO,
    @SerialName("done") val done: Boolean? = null
)

@Serializable
data class ChatMessageDTO(
    @SerialName("role") val role: Role,
    @SerialName("content") val content: String,
)

@Serializable
enum class Role {

    @SerialName("system")
    SYSTEM,

    @SerialName("user")
    USER,

    @SerialName("assistant")
    ASSISTANT,

    @SerialName("tool")
    TOOL
}

@Serializable
data class ChatOptionsDTO(
    @SerialName("temperature") val temperature: Float? = null,
    @SerialName("seed") val seed: Int? = null,
    @SerialName("num_ctx") val numContext: Int? = null
)