package org.cyanotic.olpaka.network

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import org.cyanotic.olpaka.core.domain.Model

class OllamaRestClient(
    private val client: HttpClient,
    private val decoder: Json,
    private val endpointProvider: EndpointProvider,
) {

    fun sendChatMessage(request: ChatMessageRequestDTO) = flow {
        client.preparePost {
            url(endpointProvider.generateUrl("/chat"))
            contentType(ContentType.Application.Json)
            setBody(request)
        }.streaming<ChatResponseDTO>().collect(this)
    }

    fun generate(request: GenerateRequestDTO) = flow {
        client.preparePost {
            url(endpointProvider.generateUrl("/generate"))
            contentType(ContentType.Application.Json)
            setBody(request)
        }.streaming<GenerateResponseDTO>().collect(this)
    }

    fun downloadModel(request: DownloadModelRequestDTO) = flow {
        client.preparePost {
            url(endpointProvider.generateUrl("/pull"))
            contentType(ContentType.Application.Json)
            setBody(request)
        }.streaming<DownloadModelResponseDTO>().collect(this)
    }

    suspend fun removeModel(request: RemoveModelRequestDTO): Result<Unit> {
        return try {
            val response = client.delete {
                url(endpointProvider.generateUrl("/delete"))
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(RuntimeException("Unable to remove model ${request.model}"))
            }
        } catch (e: Throwable) {
            Napier.e(
                tag = "Ollama",
                message = "Error removing model ${request.model}",
                throwable = e
            )
            Result.failure(e)
        }
    }

    suspend fun listModels(): Result<List<Model.Cached>> {
        return try {
            val response = client.get {
                url(endpointProvider.generateUrl("/tags"))
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) {
                val models = response.body<GetModelResponseDTO>().models ?: emptyList()
                Result.success(models.map { it.toModel() })
            } else {
                Result.failure(RuntimeException("Http response: 404"))
            }
        } catch (e: Throwable) {
            Napier.e(
                tag = "Ollama",
                message = "Error listing models",
                throwable = e
            )
            Result.failure(e)
        }
    }

    private inline fun <reified T> HttpStatement.streaming() = flow {
        execute {
            val channel = it.bodyAsChannel()
            while (!channel.isClosedForRead) {
                channel.readUTF8Line()?.let { line ->
                    emit(decoder.decodeFromString<T>(line))
                }
            }
        }
    }.onStart {
        Napier.d(tag = "Ollama", message = "Start streaming")
    }.onEach { element ->
        Napier.d(tag = "Ollama", message = element.toString())
    }.onCompletion { throwable ->
        throwable?.let {
            Napier.e(tag = "Ollama", message = "Complete streaming", throwable = throwable)
        } ?: Napier.i(tag = "Ollama", message = "Complete streaming")

    }

    private fun ModelDTO.toModel() = Model.Cached(
        id = this.model,
        name = this.model.modelFriendlyName(),
        size = this.size,
        quantization = this.details.quantization,
        parameters = this.details.parameters,
    )


    private fun String.modelFriendlyName(): String {
        return this.split(":").first()
    }

}