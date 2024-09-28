package org.cyanotic.olpaka.network

import io.github.aakira.napier.Napier
import io.ktor.http.*

class EndpointProvider(
    baseUrl: Url,
) {
    var baseUrl: Url = baseUrl
        set(value) {
            Napier.d("Updated baseUrl to $value", tag = "EndpointProvider")
            field = value
        }

    fun generateUrl(endpoint: String): String {
        return "$baseUrl$endpoint"
    }

    companion object {
        const val DEFAULT_OLLAMA_API_URL = "http://localhost:11434/api"
    }

}