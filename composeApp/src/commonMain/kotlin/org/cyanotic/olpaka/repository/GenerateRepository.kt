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

class GenerateRepository(
    private val client: HttpClient,
    private val decoder: Json
) {

    fun generate(query: String, model: String, context: List<Int>?) = flow {
        val request = GenerateRequestDTO(
            context = context,
            model = model,
            prompt = query,
            stream = true,
            system = "",
            temperature = 0.8,
        )
        client.preparePost {
            url("http://localhost:11434/api/generate")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.execute { resp ->
            val channel = resp.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line()
                if (line != null) {
                    val chunk = decoder.decodeFromString<GenerateResponseDTO>(line)
                    emit(chunk)
                }
            }
        }
    }

}


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