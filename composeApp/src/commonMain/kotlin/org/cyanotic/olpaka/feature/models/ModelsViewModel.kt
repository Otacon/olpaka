package org.cyanotic.olpaka.feature.models

import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.models_state_download
import olpaka.composeapp.generated.resources.models_state_initializing
import org.cyanotic.olpaka.core.ModelDownloadState
import org.cyanotic.olpaka.core.OlpakaViewModel
import org.cyanotic.olpaka.repository.DownloadModelProgress
import org.cyanotic.olpaka.repository.GetModelsResult
import org.cyanotic.olpaka.repository.ModelDTO
import org.cyanotic.olpaka.repository.ModelsRepository
import org.jetbrains.compose.resources.getString
import kotlin.math.ln
import kotlin.math.pow

class ModelsViewModel(
    private val repository: ModelsRepository,
    private val modelDownloadState: ModelDownloadState
) : OlpakaViewModel() {

    private val _state = MutableStateFlow<ModelsState>(ModelsState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ModelsEvent>()
    val event = _events.asSharedFlow()

    private var cancelDownload: Boolean = false

    override fun onCreate() = inBackground {
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
        var currentState = when (val current = _state.value) {
            is ModelsState.Content -> current.copy(isLoading = true)
            ModelsState.Error,
            ModelsState.Loading -> ModelsState.Content(isLoading = true)
        }
        repository.downloadModel(tag)
            .onStart {
                Napier.i("Started downloading...")
                cancelDownload = false
                val newModels = currentState.models + ModelUI.Downloading(
                    key = tag,
                    title = tag,
                    subtitle = getString(Res.string.models_state_initializing),
                    progress = null
                )
                currentState = currentState.copy(models = newModels)
                _state.value = currentState
                modelDownloadState.setDownloading()
            }
            .onCompletion {
                Napier.i("Completed downloading...")
                viewModelScope.launch {
                    refreshModels()
                    modelDownloadState.setCompleted()
                }
            }
            .collect { chunk ->
                Napier.i("New chunk $chunk")
                if (cancelDownload) {
                    Napier.i("Canceling download...")
                    this.cancel()
                    return@collect
                }
                val newModels = currentState.models.map { model ->
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
                currentState = currentState.copy(models = newModels, isLoading = true)
                _state.value = currentState
            }
    }

    fun onCancelDownload() {
        cancelDownload = true
    }

    fun onRemoveModelClicked(model: ModelUI.Available) = inBackground {
        _events.emit(ModelsEvent.OpenRemoveModelDialog(model.key))
    }

    fun onConfirmRemoveModel(modelKey: String) = inBackground {
        repository.removeModel(tag = modelKey)
        refreshModels()
    }

    private suspend fun refreshModels() {
        val initialState = _state.value
        _state.value = when (initialState) {
            is ModelsState.Content -> initialState.copy(isLoading = true)
            ModelsState.Error,
            ModelsState.Loading -> ModelsState.Loading
        }

        when (val result = repository.getModels()) {
            is GetModelsResult.Success -> {
                _state.value = when(initialState){
                    is ModelsState.Content -> initialState.copy(models = result.models.toModelUI(), isLoading = false)
                    ModelsState.Error,
                    ModelsState.Loading -> ModelsState.Content(result.models.toModelUI())
                }
            }
            GetModelsResult.Failure -> {
                _state.value = ModelsState.Error
            }
        }
    }

    private fun List<ModelDTO>.toModelUI(): List<ModelUI> {
        return this.map {
            val subtitle = listOfNotNull(
                it.size.toHumanReadableByteCount(),
                it.details.quantization,
                it.details.parameters
            ).joinToString(" • ")
            ModelUI.Available(
                key = it.tag,
                title = "${it.tag.modelFriendlyName()} (${it.tag})",
                subtitle = subtitle
            )
        }
    }

}

sealed interface ModelsEvent {
    data class OpenRemoveModelDialog(val key: String): ModelsEvent
    data object OpenAddModelDialog : ModelsEvent
}

sealed interface ModelsState {

    data class Content(
        val models: List<ModelUI> = emptyList(),
        val isLoading: Boolean = false,
    ) : ModelsState

    data object Loading : ModelsState

    data object Error : ModelsState
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

private fun Long.toHumanReadableByteCount(): String {
    val unit = 1000
    if (this < unit) return "$this B"
    val exp = (ln(this.toDouble()) / ln(unit.toDouble())).toInt()
    val prefixes = "kMGTPE"
    val pre = prefixes[exp - 1]
    val result = this / unit.toDouble().pow(exp)
    val roundedResult = (result * 10).toInt() / 10.0
    return "$roundedResult ${pre}B"
}

private fun String.modelFriendlyName(): String {
    return this.split(":").first()
}