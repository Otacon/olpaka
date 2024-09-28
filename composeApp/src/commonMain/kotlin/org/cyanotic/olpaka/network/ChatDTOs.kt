package org.cyanotic.olpaka.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


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