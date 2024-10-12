package org.cyanotic.olpaka.core.domain


sealed interface Model {

    val id: String

    data class Downloading(
        override val id: String,
        val name: String,
        val downloadedBytes: Long,
        val sizeBytes: Long,
        val speedBytesSecond: Long,
        val timeLeftSeconds: Long
    ) : Model

    data class Cached(
        override val id: String,
        val name: String,
        val size: Long,
        val quantization: String,
        val parameters: String
    ) : Model

    data class Error(
        override val id: String,
        val name: String,
        val message: String,
    ) : Model
}