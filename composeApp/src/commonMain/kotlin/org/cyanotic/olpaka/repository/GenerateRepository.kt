package org.cyanotic.olpaka.repository

import kotlinx.coroutines.flow.flow
import org.cyanotic.olpaka.network.GenerateRequestDTO
import org.cyanotic.olpaka.network.OllamaRestClient

class GenerateRepository(
    private val client: OllamaRestClient,
) {

    fun generate(
        query: String,
        model: String,
        context: List<Int>?
    ) = flow {
        val request = GenerateRequestDTO(
            context = context,
            model = model,
            prompt = query,
            stream = true,
            system = "",
            temperature = 0.8,
        )
        client.generate(request).collect(this)
    }

}

