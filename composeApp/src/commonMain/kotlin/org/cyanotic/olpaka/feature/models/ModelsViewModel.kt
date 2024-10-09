package org.cyanotic.olpaka.feature.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import org.cyanotic.olpaka.core.DownloadFormatter
import org.cyanotic.olpaka.core.FirebaseAnalytics
import org.cyanotic.olpaka.core.ModelDownloadState
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.core.inBackground
import org.cyanotic.olpaka.core.toHumanReadableByteCount
import org.cyanotic.olpaka.repository.DownloadModelProgress
import org.cyanotic.olpaka.repository.ModelsRepository

class ModelsViewModel(
    private val repository: ModelsRepository,
    private val modelDownloadState: ModelDownloadState,
    private val analytics: FirebaseAnalytics,
    private val downloadFormatter: DownloadFormatter,
) : ViewModel() {

    private val _state = MutableStateFlow<ModelsState>(ModelsState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ModelsEvent>()
    val event = _events.asSharedFlow()

    private val stateMutex = Mutex()

    private var cachedModels = listOf<Model.Cached>()
    private var downloadingModel: ModelUI? = null
    private var downloadProcess: Job? = null

    fun onCreate() = inBackground {
        analytics.screenView("models")
        refreshModels()
    }

    fun onRefreshClicked() = inBackground {
        analytics.event("refresh_models")
        refreshModels()
    }

    fun onAddModelClicked() = inBackground {
        _events.emit(ModelsEvent.OpenAddModelDialog)
    }

    fun onAddModel(tag: String) {
        _state.value = ModelsState.Content(
            models = cachedModels.toModelUI(),
            controlsEnabled = false
        )
        var lastUpdateTime = Clock.System.now().toEpochMilliseconds()
        var previousDownloadedBytes = 0L
        downloadProcess = viewModelScope.launch {
            repository.downloadModel(tag)
                .cancellable()
                .onStart {
                    stateMutex.withLock {
                        val newModel = ModelUI.Downloading(
                            key = tag,
                            title = tag,
                            subtitle = "Downloading...",
                            progress = null
                        )
                        val newModels = cachedModels.mapNotNull {
                            if (it.id == tag) {
                                null
                            } else {
                                it
                            }
                        }
                        downloadingModel = newModel
                        val modelsUi = (newModels).toModelUI() + newModel
                        _state.value = ModelsState.Content(
                            models = modelsUi,
                            controlsEnabled = false
                        )
                        modelDownloadState.setDownloading()
                    }
                }
                .onCompletion { throwable ->
                    Napier.i(
                        tag = "ModelsViewModel",
                        message = "Download completed",
                        throwable = throwable
                    )
                    if (throwable != null) {
                        downloadingModel = ModelUI.Error(
                            key = tag,
                            title = tag,
                            subtitle = throwable.message ?: "UnknownError",
                        )
                    }
                    analytics.event(
                        eventName = "download_model",
                        properties = mapOf("model" to tag)
                    )
                    viewModelScope.launch {
                        refreshModels()
                        modelDownloadState.setCompleted()
                    }
                }
                .collect { chunk ->
                    stateMutex.withLock {
                        val newModel = chunk.toModelUI(
                            previousDownloadedBytes = previousDownloadedBytes,
                            previousUpdateTime = lastUpdateTime,
                            setLastDownloadedBytes = { bytes -> previousDownloadedBytes = bytes },
                            setLastUpdateTime = { time -> lastUpdateTime = time }
                        )
                        Napier.d(tag = "ModelsViewModel", message = "chunk $chunk")
                        newModel?.let {
                            downloadingModel = it
                            val newModels = (cachedModels.toModelUI() + it)
                            _state.value = ModelsState.Content(
                                models = newModels,
                                controlsEnabled = false
                            )
                        }
                    }
                }
        }
    }

    fun onCancelDownload() {
        downloadProcess?.cancel()
    }

    fun onRemoveModelClicked(model: ModelUI.Available) = inBackground {
        _events.emit(ModelsEvent.OpenRemoveModelDialog(model.key))
    }

    fun onConfirmRemoveModel(modelKey: String) = inBackground {
        _state.value = ModelsState.Content(
            models = cachedModels.toModelUI(),
            controlsEnabled = false
        )
        repository.removeModel(tag = modelKey)
        analytics.event("remove_model", properties = mapOf("model" to modelKey))
        refreshModels()
    }

    private suspend fun refreshModels() {
        _state.value = if (cachedModels.isEmpty()) {
            ModelsState.Loading
        } else {
            ModelsState.Content(
                models = cachedModels.toModelUI() + downloadingModel,
                controlsEnabled = false,
            )
        }
        stateMutex.withLock {
            val result = repository.getModels()
            if (result.isSuccess) {
                val models = result.getOrThrow()
                cachedModels = models
                val newModelsUI = downloadingModel?.let { downloading ->
                    Napier.i(tag = "ModelsViewModel", message = "Currenlty downloading model")
                    val completedDownload = models.any { it.id == downloading.key }
                    if (completedDownload) {
                        downloadingModel = null
                        models.toModelUI()
                    } else {
                        models.toModelUI() + downloading
                    }
                } ?: models.toModelUI()
                Napier.d(tag = "ModelsViewModel", message = "Downloading Model: $downloadingModel")
                _state.value = ModelsState.Content(
                    models = newModelsUI,
                    controlsEnabled = true
                )
            } else {
                cachedModels = emptyList()
                _state.value = ModelsState.Error(
                    title = "errorTitle",
                    message = "error message"
                )
            }
        }
    }


    private fun DownloadModelProgress.toModelUI(
        previousDownloadedBytes: Long,
        previousUpdateTime: Long,
        setLastDownloadedBytes: (Long) -> Unit = {},
        setLastUpdateTime: (Long) -> Unit = {},
    ): ModelUI? {
        return when (this) {
            is DownloadModelProgress.Downloading -> {
                val progress = if (total > 0 && completed > 0) {
                    completed / total.toFloat()
                } else {
                    null
                }
                val now = Clock.System.now().toEpochMilliseconds()
                if (now - previousUpdateTime < 1000) {
                    return null
                }
                val subtitle = downloadFormatter.formatDownloadProgress(
                    totalBytes = total,
                    downloadedBytes = completed,
                    previousBytesDownloaded = previousDownloadedBytes,
                    previousUpdateTime = previousUpdateTime
                )

                setLastDownloadedBytes(completed)
                setLastUpdateTime(now)

                ModelUI.Downloading(
                    key = tag,
                    title = tag,
                    subtitle = subtitle,
                    progress = progress,
                )
            }

            is DownloadModelProgress.Processing -> ModelUI.Downloading(
                key = tag,
                title = tag,
                subtitle = status,
                progress = null,
            )

            is DownloadModelProgress.Error -> ModelUI.Error(
                key = tag,
                title = tag,
                subtitle = this.message ?: "Unknown Error",
            )

        }

    }
}

private operator fun List<ModelUI>.plus(otherModel: ModelUI?): List<ModelUI> {
    return if (otherModel == null) {
        this
    } else {
        this.toMutableList().also { it.add(otherModel) }
    }
}

private fun List<Model.Cached>.toModelUI() = map { it.toModelUI() }

private fun Model.Cached.toModelUI(): ModelUI {
    val subtitle = listOfNotNull(
        size.toHumanReadableByteCount(),
        quantization,
        parameters
    ).joinToString(" • ")
    return ModelUI.Available(
        key = id,
        title = "$name ($id)",
        subtitle = subtitle
    )
}

sealed interface ModelsEvent {
    data class OpenRemoveModelDialog(val key: String) : ModelsEvent
    data object OpenAddModelDialog : ModelsEvent
}

sealed interface ModelsState {

    data class Content(
        val models: List<ModelUI> = emptyList(),
        val controlsEnabled: Boolean = false,
    ) : ModelsState

    data object Loading : ModelsState

    data class Error(
        val title: String,
        val message: String
    ) : ModelsState

}

sealed interface ModelUI {

    val key: String

    data class Downloading(
        override val key: String,
        val title: String,
        val subtitle: String,
        val progress: Float?,
    ) : ModelUI

    data class Available(
        override val key: String,
        val title: String,
        val subtitle: String,
    ) : ModelUI

    data class Error(
        override val key: String,
        val title: String,
        val subtitle: String,
    ) : ModelUI
}

