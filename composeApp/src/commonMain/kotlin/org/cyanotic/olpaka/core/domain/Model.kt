package org.cyanotic.olpaka.core.domain

data class Model(
    val id: String,
    val name: String,
    val size: Long,
    val quantization: String,
    val parameters: String
)