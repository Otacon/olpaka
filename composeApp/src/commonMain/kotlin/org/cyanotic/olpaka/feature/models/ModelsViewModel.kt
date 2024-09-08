package org.cyanotic.olpaka.feature.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.cyanotic.olpaka.repository.DownloadModelProgress
import org.cyanotic.olpaka.repository.ModelsRepository

class ModelsViewModel(
    private val repository: ModelsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ModelsState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ModelsEvent>()
    val event = _events.asSharedFlow()

    private var cancelDownload: Boolean = false

    fun onCreate() = viewModelScope.launch(Dispatchers.Default) {
        refreshModels()
    }

    fun onRefreshClicked() = viewModelScope.launch(Dispatchers.Default) {
        refreshModels()
    }

    fun onAddModelClicked() = viewModelScope.launch(Dispatchers.Default) {
        _events.emit(ModelsEvent.ShowAddModelDialog)
    }

    fun onAddModel(tag: String) = viewModelScope.launch(Dispatchers.Default) {
        repository.downloadModel(tag)
            .onStart {
                cancelDownload = false
                val newModels = _state.value.models + ModelUI.Downloading(
                    key = tag,
                    title = tag,
                    subtitle = "Initializing...",
                    progress = null
                )
                _state.value = _state.value.copy(models = newModels)
            }
            .onCompletion {
                viewModelScope.launch { refreshModels() }
            }
            .collect { chunk ->
                if (cancelDownload) {
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
                                    model.copy(subtitle = "Downloading...", progress = progress)
                                }

                                is DownloadModelProgress.Processing -> {
                                    val status = chunk.status.replaceFirstChar { it.uppercaseChar() }
                                    model.copy(subtitle = status, progress = null)
                                }
                            }
                        }
                    }
                }
                _state.value = _state.value.copy(models = newModels)
            }
    }

    fun onCancelDownload(model: ModelUI.Downloading) {
        cancelDownload = true
    }

    fun onRemoveModel(model: ModelUI.Available) = viewModelScope.launch(Dispatchers.Default) {
        val removed = repository.removeModel(tag = model.title)
        if (removed) {
            refreshModels()
        }
    }

    private suspend fun refreshModels() {
        val models = repository.getModels().map {
            ModelUI.Available(
                key = it.tag,
                title = it.name,
                subtitle = "${it.tag} ${it.details.quantization}"
            )
        }
        _state.getAndUpdate { current ->
            current.copy(models = models)
        }
    }


}

sealed interface ModelsEvent {
    data object ShowAddModelDialog : ModelsEvent
}

data class ModelsState(
    val models: List<ModelUI> = emptyList()

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