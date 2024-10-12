package org.cyanotic.olpaka.repository

import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import org.cyanotic.olpaka.core.DownloadStatsCalculator
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.network.DownloadModelRequestDTO
import org.cyanotic.olpaka.network.OllamaRestClient
import org.cyanotic.olpaka.network.RemoveModelRequestDTO

interface ModelsRepository {

    val models: StateFlow<List<Model>>

    suspend fun getModels(): Result<List<Model.Cached>>

    suspend fun removeModel(tag: String): Result<Unit>

    suspend fun downloadModel(tag: String)

    fun cancelDownload()

}

class ModelsRepositoryDefault(
    private val restClient: OllamaRestClient,
    private val downloadStatsCalculator: DownloadStatsCalculator,
) : ModelsRepository {

    private var cancelDownload: Boolean = false
    private val _models = MutableStateFlow(listOf<Model>())
    override val models = _models.asStateFlow()

    private val mutex = Mutex()

    override suspend fun getModels(): Result<List<Model.Cached>> = mutex.withLock {
        refreshModels()
        return Result.success(_models.value.filterIsInstance<Model.Cached>())
    }

    override suspend fun removeModel(tag: String): Result<Unit> = mutex.withLock {
        val result = restClient.removeModel(RemoveModelRequestDTO(tag))
        refreshModels()
        return result
    }

    override suspend fun downloadModel(tag: String) {
        val request = DownloadModelRequestDTO(tag)
        var previousBytesDownloaded = 0L
        var lastUpdateTime = Clock.System.now().toEpochMilliseconds()
        restClient.downloadModel(request)
            .cancellable()
            .onStart {
                mutex.withLock {
                    cancelDownload = false
                    val downloadingModel = Model.Downloading(
                        id = tag,
                        name = tag,
                        sizeBytes = 0,
                        downloadedBytes = 0,
                        speedBytesSecond = 0,
                        timeLeftSeconds = 0
                    )
                    _models.getAndUpdate { current ->
                        var replaced = false
                        val newModels = current.replaceById(tag) {
                            replaced = true
                            downloadingModel
                        }
                        if (replaced) {
                            newModels
                        } else {
                            current + downloadingModel
                        }
                    }
                }
            }
            .onCompletion { throwable ->
                mutex.withLock {
                    val updatedModel = if (throwable != null) {
                        Model.Error(
                            id = tag,
                            name = tag,
                            message = throwable.message ?: ""
                        )
                    } else {
                        Model.Cached(
                            id = tag,
                            name = tag,
                            size = 0,
                            quantization = "",
                            parameters = ""
                        )
                    }
                    val updatedModels = _models.value.replaceById(tag) { updatedModel }
                    _models.value = updatedModels
                    refreshModels()
                }
            }
            .collect { chunk ->
                if (cancelDownload) {
                    currentCoroutineContext().cancel()
                }
                val model = when {
                    chunk.error != null -> throw IllegalStateException(chunk.error)

                    chunk.total != null && chunk.completed != null -> {
                        val now = Clock.System.now().toEpochMilliseconds()
                        if (now - lastUpdateTime < 1_000) {
                            return@collect
                        }
                        val speedBytesSecond = downloadStatsCalculator.calculateDownloadSpeed(
                            downloadedBytes = chunk.completed,
                            previousBytesDownloaded = previousBytesDownloaded,
                            previousUpdateTime = lastUpdateTime,
                        )
                        val timeLeftSecond = downloadStatsCalculator.calculateRemainingTime(
                            totalBytes = chunk.total,
                            downloadedBytes = chunk.completed,
                            previousBytesDownloaded = previousBytesDownloaded,
                            previousUpdateTime = lastUpdateTime
                        )
                        lastUpdateTime = now
                        previousBytesDownloaded = chunk.completed
                        Model.Downloading(
                            id = tag,
                            name = tag,
                            sizeBytes = chunk.total,
                            downloadedBytes = chunk.completed,
                            speedBytesSecond = speedBytesSecond ?: 0,
                            timeLeftSeconds = timeLeftSecond ?: 0
                        )
                    }

                    else -> Model.Downloading(
                        id = tag,
                        name = tag,
                        sizeBytes = 0,
                        downloadedBytes = 0,
                        speedBytesSecond = 0,
                        timeLeftSeconds = 0
                    )
                }
                _models.getAndUpdate { it.replaceById(tag) { model } }
            }
    }

    override fun cancelDownload() {
        cancelDownload = true
    }

    private suspend fun refreshModels() {
        val result = restClient.listModels()
        if (result.isSuccess) {
            val value = result.getOrThrow()
            _models.getAndUpdate { currentModels ->
                val cachedModels = currentModels.filterIsInstance<Model.Cached>().toSet()
                val nonCachedModels = currentModels - cachedModels
                value + nonCachedModels
            }
        }
    }
}

private fun List<Model>.replaceById(id: String, block: (Model) -> Model?): List<Model> {
    return this.mapNotNull {
        if (it.id == id) {
            block(it)
        } else {
            it
        }
    }
}