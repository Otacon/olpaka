package org.cyanotic.olpaka.network

import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.repository.*

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
            if( response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(RuntimeException("Unable to remove model ${request.model}"))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun listModels(): Result<List<Model>> {
        return try {
            val response = client.get {
                url(endpointProvider.generateUrl("/tags"))
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) {
                val models = response.body<GetModelResponseDTO>().models ?: emptyList()
                Result.success(models.map{it.toModel()})
            } else {
                Result.failure(RuntimeException("Http response: 404"))
            }
        } catch (e: Throwable) {
            Napier.e(message = "Throwable - ${e::class} - Message ${e.message} - Cause ${e.cause}")
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