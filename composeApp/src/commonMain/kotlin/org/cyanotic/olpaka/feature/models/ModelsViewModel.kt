package org.cyanotic.olpaka.feature.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
) : ViewModel() {

    private val _state = MutableStateFlow<ModelsState>(ModelsState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ModelsEvent>()
    val event = _events.asSharedFlow()

    private val stateMutex = Mutex()

    private var cachedModels = listOf<Model.Cached>()
    private var downloadingModel :Model.Downloading? = null
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

        downloadProcess = viewModelScope.launch {
            repository.downloadModel(tag)
                .cancellable()
                .onStart {
                    stateMutex.withLock {
                        val newModel = Model.Downloading(
                            id = tag,
                            name = tag,
                            downloaded = 0,
                            size = 0
                        )
                        val newModels = cachedModels.mapNotNull {
                            if (it.id == tag) {
                                null
                            } else {
                                it
                            }
                        }
                        downloadingModel = newModel
                        val modelsUi = (newModels + newModel).toModelUI()
                        _state.value = ModelsState.Content(
                            models = modelsUi,
                            controlsEnabled = false
                        )
                        modelDownloadState.setDownloading()
                    }
                }
                .onCompletion {
                    downloadingModel = null
                    analytics.event(eventName = "download_model", properties = mapOf("model" to tag))
                    viewModelScope.launch {
                        refreshModels()
                        modelDownloadState.setCompleted()
                    }
                }
                .catch {
                    val newModels = cachedModels.toModelUI()
                    downloadingModel = null
                    // TODO handle model error
                    _state.value = ModelsState.Content(
                        models = newModels,
                        controlsEnabled = true
                    )
                }
                .collect { chunk ->
                    val newModel = when (chunk) {
                        is DownloadModelProgress.Downloading -> {
                            Model.Downloading(
                                id = tag,
                                name = tag,
                                downloaded = chunk.completed,
                                size = chunk.total
                            )
                        }

                        is DownloadModelProgress.Processing -> {
                            //TODO handle status
                            val status = chunk.status.replaceFirstChar { it.uppercaseChar() }
                            Model.Downloading(
                                id = tag,
                                name = tag,
                                downloaded = null,
                                size = null
                            )
                        }
                    }
                    downloadingModel = newModel
                    val newModels = (cachedModels + newModel).toModelUI()
                    _state.value = ModelsState.Content(models = newModels, controlsEnabled = false)
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
                models = (cachedModels + downloadingModel).filterNotNull().toModelUI(),
                controlsEnabled = false,
            )
        }
        stateMutex.withLock {
            val result = repository.getModels()
            if (result.isSuccess) {
                val models = result.getOrThrow().filterIsInstance<Model.Cached>()
                cachedModels = models
                _state.value = ModelsState.Content(
                    models = models.toModelUI(),
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

}

private fun List<Model>.toModelUI(): List<ModelUI> {
    return this.map { it.toModelUI() }
}

private fun Model.toModelUI(): ModelUI {
    return when (this) {
        is Model.Cached -> {
            val subtitle = listOfNotNull(
                size.toHumanReadableByteCount(),
                quantization,
                parameters
            ).joinToString(" • ")
            ModelUI.Available(
                key = id,
                title = "$name ($id)",
                subtitle = subtitle
            )
        }

        is Model.Downloading -> {
            val progress = if (size != null && size > 0 && downloaded != null && downloaded > 0) {
                downloaded / size.toFloat()
            } else {
                null
            }
            ModelUI.Downloading(
                key = id,
                title = name,
                subtitle = "Downloading",
                progress = progress
            )
        }
    }

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

