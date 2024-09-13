package org.cyanotic.olpaka.repository

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.cyanotic.olpaka.network.OllamaRestClient

class ModelsRepository(
    private val restClient: OllamaRestClient,
) {

    suspend fun getModels(): GetModelsResult {
        return restClient.listModels()
    }

    suspend fun removeModel(tag: String): Boolean {
        return restClient.removeModel(RemoveModelRequestDTO(tag))
    }

    fun downloadModel(tag: String) = flow {
        val request = DownloadModelRequestDTO(tag)
        restClient.downloadModel(request)
            .map { chunk ->
                when {
                    chunk.total != null && chunk.completed != null -> {
                        DownloadModelProgress.Downloading(chunk.total, chunk.completed)
                    }

                    else -> DownloadModelProgress.Processing(chunk.status ?: "")
                }
            }
            .collect(this)
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