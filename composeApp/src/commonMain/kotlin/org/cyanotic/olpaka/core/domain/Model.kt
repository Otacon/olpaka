package org.cyanotic.olpaka.core.domain


sealed interface Model {

    data class Downloading(
        val id: String,
        val name: String,
        val downloaded: Long?,
        val size: Long?,
    ) : Model

    data class Cached(
        val id: String,
        val name: String,
        val size: Long,
        val quantization: String,
        val parameters: String
    ) : Model
}