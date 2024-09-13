package org.cyanotic.olpaka.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.flow
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
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

    suspend fun removeModel(request: RemoveModelRequestDTO): Boolean {
        val response = client.delete {
            url(endpointProvider.generateUrl("/delete"))
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.status.isSuccess()
    }

    suspend fun listModels(): GetModelsResult {
        return try {
            val response = client.get {
                url(endpointProvider.generateUrl("/tags"))
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                val models = response.body<GetModelResponseDTO>().models ?: emptyList()
                GetModelsResult.Success(models)
            } else {
                GetModelsResult.Failure
            }
        } catch (e: IOException) {
            GetModelsResult.Failure
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

}