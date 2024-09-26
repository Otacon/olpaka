package org.cyanotic.olpaka.feature.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.models_state_download
import olpaka.composeapp.generated.resources.models_state_initializing
import org.cyanotic.olpaka.core.Analytics
import org.cyanotic.olpaka.core.ModelDownloadState
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.core.inBackground
import org.cyanotic.olpaka.core.toHumanReadableByteCount
import org.cyanotic.olpaka.repository.DownloadModelProgress
import org.cyanotic.olpaka.repository.ModelsRepository
import org.jetbrains.compose.resources.getString

class ModelsViewModel(
    private val repository: ModelsRepository,
    private val modelDownloadState: ModelDownloadState,
    private val analytics: Analytics,
) : ViewModel() {

    private val _state = MutableStateFlow(ModelsState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ModelsEvent>()
    val event = _events.asSharedFlow()

    private var cancelDownload: Boolean = false

    fun onCreate() = inBackground {
        analytics.trackScreenView("models")
        refreshModels()
    }

    fun onRefreshClicked() = inBackground {
        refreshModels()
    }

    fun onAddModelClicked() = inBackground {
        _events.emit(ModelsEvent.OpenAddModelDialog)
    }

    fun onAddModel(tag: String) = inBackground {
        Napier.i("Downloading model $tag")
        _state.value = _state.value.copy(isLoading = true)
        repository.downloadModel(tag)
            .onStart {
                Napier.i("Started downloading...")
                cancelDownload = false

                val downloadingModel = ModelUI.Downloading(
                    key = tag,
                    title = tag,
                    subtitle = getString(Res.string.models_state_initializing),
                    progress = null
                )

                var modelFound = false
                var newModels = _state.value.models.map {
                    if (it.key == tag) {
                        modelFound = true
                        downloadingModel
                    } else {
                        it
                    }
                }
                newModels = if (!modelFound) {
                    newModels + downloadingModel
                } else {
                    newModels
                }
                _state.value = _state.value.copy(models = newModels)
                modelDownloadState.setDownloading()
            }
            .onCompletion {
                Napier.i("Completed downloading...")
                viewModelScope.launch {
                    refreshModels()
                    modelDownloadState.setCompleted()
                }
            }
            .catch {
                val newModels = _state.value.models.map { model ->
                    when (model) {
                        is ModelUI.Available,
                        is ModelUI.Error -> model

                        is ModelUI.Downloading -> {
                            // TODO fix error state
                            ModelUI.Error(model.key, model.title, "Error")
                        }
                    }
                }
                _state.value = _state.value.copy(models = newModels, isLoading = true)
            }
            .collect { chunk ->
                Napier.i("New chunk $chunk")
                if (cancelDownload) {
                    Napier.i("Canceling download...")
                    this.cancel()
                    return@collect
                }
                val newModels = _state.value.models.map { model ->
                    when (model) {
                        is ModelUI.Available,
                        is ModelUI.Error -> model

                        is ModelUI.Downloading -> {
                            when (chunk) {
                                is DownloadModelProgress.Downloading -> {
                                    val progress = chunk.completed.toFloat() / chunk.total
                                    model.copy(
                                        subtitle = getString(Res.string.models_state_download),
                                        progress = progress
                                    )
                                }

                                is DownloadModelProgress.Processing -> {
                                    val status = chunk.status.replaceFirstChar { it.uppercaseChar() }
                                    model.copy(
                                        subtitle = status,
                                        progress = null
                                    )
                                }
                            }
                        }
                    }
                }
                _state.value = _state.value.copy(models = newModels, isLoading = true)
            }
    }

    fun onCancelDownload() {
        cancelDownload = true
    }

    fun onRemoveModelClicked(model: ModelUI.Available) = inBackground {
        _events.emit(ModelsEvent.OpenRemoveModelDialog(model.key))
    }

    fun onConfirmRemoveModel(modelKey: String) = inBackground {
        _state.value = _state.value.copy(isLoading = true)
        repository.removeModel(tag = modelKey)
        refreshModels()
    }

    private suspend fun refreshModels() {
        _state.value = _state.value.copy(isLoading = true)
        repository.getModels()
            .onSuccess {
                val newState = _state.value.copy(models = it.toModelUI(), isLoading = false, error = false)
                Napier.d(tag = "ModelsViewModel", message = "Refresh -> $newState")
                _state.value = newState
            }
            .onFailure {
                _state.value = _state.value.copy(isLoading = false, error = true)
            }
    }

    private fun List<Model>.toModelUI(): List<ModelUI> {
        return this.map {
            val subtitle = listOfNotNull(
                it.size.toHumanReadableByteCount(),
                it.quantization,
                it.parameters
            ).joinToString(" • ")
            ModelUI.Available(
                key = it.id,
                title = "${it.name} (${it.id})",
                subtitle = subtitle
            )
        }
    }

}

sealed interface ModelsEvent {
    data class OpenRemoveModelDialog(val key: String) : ModelsEvent
    data object OpenAddModelDialog : ModelsEvent
}

data class ModelsState(
    val models: List<ModelUI> = emptyList(),
    val isLoading: Boolean = false,
    val error: Boolean = false,
)

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

