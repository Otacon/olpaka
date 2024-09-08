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

    fun generate(query: String, model: String) = flow {
        val request = GenerateRequestDTO(
            stream = true,
            system = "",
            model = model,
            temperature = 0.8,
            prompt = query
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
    @SerialName("stream") val stream: Boolean,
    @SerialName("system") val system: String,
    @SerialName("model") val model: String,
    @SerialName("temperature") val temperature: Double,
    @SerialName("prompt") val prompt: String
)

@Serializable
data class GenerateResponseDTO(
    @SerialName("model") val model: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("response") val response: String,
    @SerialName("done") val done: Boolean,
    @SerialName("done_reason") val doneReason: String? = null,
    @SerialName("load_duration") val loadDuration: Long? = null,
    @SerialName("prompt_eval_count") val promptEvalCount: Int? = null,
    @SerialName("prompt_eval_duration") val promptEvalDuration: Long? = null,
    @SerialName("eval_count") val evalCount: Int? = null,
    @SerialName("eval_duration") val evalDuration: Long? = null,
)