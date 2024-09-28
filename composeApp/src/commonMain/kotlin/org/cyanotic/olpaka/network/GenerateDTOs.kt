package org.cyanotic.olpaka.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerateRequestDTO(
    @SerialName("context") val context: List<Int>?,
    @SerialName("model") val model: String,
    @SerialName("prompt") val prompt: String,
    @SerialName("stream") val stream: Boolean,
    @SerialName("system") val system: String,
    @SerialName("temperature") val temperature: Double
)

@Serializable
data class GenerateResponseDTO(
    @SerialName("context") val context: List<Int>? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("done") val done: Boolean,
    @SerialName("done_reason") val doneReason: String? = null,
    @SerialName("eval_count") val evalCount: Int? = null,
    @SerialName("eval_duration") val evalDuration: Long? = null,
    @SerialName("load_duration") val loadDuration: Long? = null,
    @SerialName("model") val model: String,
    @SerialName("prompt_eval_count") val promptEvalCount: Int? = null,
    @SerialName("prompt_eval_duration") val promptEvalDuration: Long? = null,
    @SerialName("response") val response: String,
)