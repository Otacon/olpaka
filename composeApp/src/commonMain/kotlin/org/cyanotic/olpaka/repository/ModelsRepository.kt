package org.cyanotic.olpaka.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.flow
import kotlinx.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ModelsRepository(
    private val client: HttpClient,
    private val decoder: Json
) {

    suspend fun getModels(): GetModelsResult {
        return try {
            val response = client.get {
                url("http://localhost:11434/api/tags")
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

    suspend fun removeModel(tag: String): Boolean {
        val request = RemoveModelRequestDTO(tag)
        val response = client.delete {
            url("http://localhost:11434/api/delete")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.status.isSuccess()
    }

    fun downloadModel(tag: String) = flow {
        val request = DownloadModelRequestDTO(tag)
        client.preparePost {
            url("http://localhost:11434/api/pull")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.execute { resp ->
            val channel = resp.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line()
                if (line != null) {
                    val chunk = decoder.decodeFromString<DownloadModelResponseDTO>(line)
                    when {
                        chunk.total != null && chunk.completed != null -> {
                            emit(DownloadModelProgress.Downloading(chunk.total, chunk.completed))
                        }

                        else -> emit(DownloadModelProgress.Processing(chunk.status ?: ""))
                    }
                }
            }
        }
    }


}

sealed interface GetModelsResult {
    data class Success(val models: List<ModelDTO>) : GetModelsResult
    data object Failure : GetModelsResult
}

@Serializable
data class GetModelResponseDTO(
    @SerialName("models") val models: List<ModelDTO>? = null,
)

@Serializable
data class ModelDTO(
    @SerialName("name") val name: String,
    @SerialName("model") val tag: String,
    @SerialName("size") val size: Long,
    @SerialName("details") val details: ModelDetailsDTO,
)

@Serializable
data class ModelDetailsDTO(
    @SerialName("parameter_size") val parameters: String,
    @SerialName("quantization_level") val quantization: String,
)

@Serializable
data class RemoveModelRequestDTO(
    @SerialName("model") val model: String
)

@Serializable
data class DownloadModelRequestDTO(
    @SerialName("name") val name: String,
    @SerialName("stream") val stream: Boolean = true,
)


@Serializable
data class DownloadModelResponseDTO(
    @SerialName("error") val error: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("total") val total: Long? = null,
    @SerialName("completed") val completed: Long? = null,
)

sealed interface DownloadModelProgress {
    data class Downloading(
        val total: Long,
        val completed: Long,
    ) : DownloadModelProgress

    data class Processing(
        val status: String
    ) : DownloadModelProgress
}