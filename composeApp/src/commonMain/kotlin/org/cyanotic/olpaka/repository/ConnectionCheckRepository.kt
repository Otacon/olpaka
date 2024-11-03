package org.cyanotic.olpaka.repository

import org.cyanotic.olpaka.network.OllamaRestClient

interface ConnectionCheckRepository {
    suspend fun checkConnection(): Boolean
}

class ConnectionCheckRepositoryDefault(
    private val restClient: OllamaRestClient,
) : ConnectionCheckRepository {

    override suspend fun checkConnection(): Boolean {
        return restClient.checkConnection()
    }

}