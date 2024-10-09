package org.cyanotic.olpaka.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GetModelResponseDTO(
    @SerialName("models") val models: List<ModelDTO>? = null,
)

@Serializable
data class ModelDTO(
    @SerialName("name") val name: String,
    @SerialName("model") val model: String,
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