package org.cyanotic.olpaka.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.network.DownloadModelRequestDTO
import org.cyanotic.olpaka.network.OllamaRestClient
import org.cyanotic.olpaka.network.RemoveModelRequestDTO

interface ModelsRepository {
    suspend fun getModels(): Result<List<Model.Cached>>

    suspend fun removeModel(tag: String): Result<Unit>

    fun downloadModel(tag: String): Flow<DownloadModelProgress>
}

class ModelsRepositoryDefault(
    private val restClient: OllamaRestClient,
) : ModelsRepository {

    override suspend fun getModels(): Result<List<Model.Cached>> {
        return restClient.listModels()
    }

    override suspend fun removeModel(tag: String): Result<Unit> {
        return restClient.removeModel(RemoveModelRequestDTO(tag))
    }

    override fun downloadModel(tag: String) = flow {
        val request = DownloadModelRequestDTO(tag)
        restClient.downloadModel(request).map { chunk ->
            when {
                chunk.total != null && chunk.completed != null -> DownloadModelProgress.Downloading(
                    tag = tag,
                    total = chunk.total,
                    completed = chunk.completed,
                )

                chunk.error != null -> DownloadModelProgress.Error(
                    tag = tag,
                    message = chunk.error
                )

                else -> DownloadModelProgress.Processing(
                    tag = tag,
                    status = chunk.status ?: ""
                )
            }
        }.catch { e ->
            DownloadModelProgress.Error(
                tag = tag,
                message = e.message
            )
        }.collect(this)
    }

}

sealed interface DownloadModelProgress {
    data class Downloading(
        val tag: String,
        val total: Long,
        val completed: Long,
    ) : DownloadModelProgress

    data class Processing(
        val tag: String,
        val status: String
    ) : DownloadModelProgress

    data class Error(
        val tag: String,
        val message: String?
    ) : DownloadModelProgress
}