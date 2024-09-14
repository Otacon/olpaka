package org.cyanotic.olpaka.feature.models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.models_dialog_download_model_error_already_added
import olpaka.composeapp.generated.resources.models_state_download
import olpaka.composeapp.generated.resources.models_state_initializing
import org.cyanotic.olpaka.core.ModelDownloadState
import org.cyanotic.olpaka.core.OlpakaViewModel
import org.cyanotic.olpaka.repository.DownloadModelProgress
import org.cyanotic.olpaka.repository.GetModelsResult
import org.cyanotic.olpaka.repository.ModelsRepository
import org.jetbrains.compose.resources.getString
import kotlin.math.ln
import kotlin.math.pow

class ModelsViewModel(
    private val repository: ModelsRepository,
    private val modelDownloadState: ModelDownloadState
) : OlpakaViewModel() {

    private val _state = MutableStateFlow(ModelsState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ModelsEvent>()
    val event = _events.asSharedFlow()

    private var cancelDownload: Boolean = false

    override fun onCreate() = inBackground {
        _state.getAndUpdate { current -> current.copy(isLoading = true) }
        refreshModels()
        _state.getAndUpdate { current -> current.copy(isLoading = false) }
    }

    fun onRefreshClicked() = inBackground {
        _state.getAndUpdate { current -> current.copy(isLoading = true) }
        refreshModels()
        _state.getAndUpdate { current -> current.copy(isLoading = false) }
    }

    fun onAddModelClicked() = inBackground {
        _state.getAndUpdate { current -> current.copy(addModelDialogState = AddModelDialogState()) }
    }

    fun onAddModelTextChanged(text: String) = viewModelScope.launch {
        _state.getAndUpdate { current ->
            current.addModelDialogState?.let { dialogState ->
                val alreadyDownloaded = current.models.any { it.key == text.trim() }
                val isAddEnabled = text.isNotBlank() && !alreadyDownloaded
                val error = if (alreadyDownloaded) {
                    getString(Res.string.models_dialog_download_model_error_already_added)
                } else {
                    null
                }
                val newDialogState = dialogState.copy(
                    text = text,
                    isAddEnabled = isAddEnabled,
                    error = error
                )
                current.copy(addModelDialogState = newDialogState)
            } ?: current
        }
    }

    fun onCloseAddModelDialog() {
        _state.getAndUpdate { current -> current.copy(addModelDialogState = null) }
    }

    fun onAddModel(tag: String) = inBackground {
        _state.getAndUpdate { current -> current.copy(isLoading = true, addModelDialogState = null) }
        repository.downloadModel(tag)
            .onStart {
                cancelDownload = false
                val newModels = _state.value.models + ModelUI.Downloading(
                    key = tag,
                    title = tag,
                    subtitle = getString(Res.string.models_state_initializing),
                    progress = null
                )
                _state.value = _state.value.copy(models = newModels)
                modelDownloadState.setDownloading()
            }
            .onCompletion {
                viewModelScope.launch {
                    refreshModels()
                    _state.getAndUpdate { current -> current.copy(isLoading = false) }
                    modelDownloadState.setCompleted()
                }
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
                _state.value = _state.value.copy(models = newModels)
            }
    }

    fun onCancelDownload() {
        cancelDownload = true
    }

    fun onRemoveModelClicked(model: ModelUI.Available) = inBackground {
        _state.getAndUpdate { current -> current.copy(removeModelDialogState = RemoveModelDialogState(model.key)) }
    }

    fun onRemoveModelDialogResult(remove: Boolean) = inBackground {
        val model = _state.value.removeModelDialogState?.model
        _state.getAndUpdate { current -> current.copy(removeModelDialogState = null) }
        if (!remove || model == null) {
            return@inBackground
        }
        _state.getAndUpdate { current -> current.copy(isLoading = true) }
        repository.removeModel(tag = model)
        refreshModels()
        _state.getAndUpdate { current -> current.copy(isLoading = false) }
    }

    private suspend fun refreshModels() {
        val models = when (val result = repository.getModels()) {
            is GetModelsResult.Success -> result.models.map {
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

            GetModelsResult.Failure -> emptyList()
        }
        _state.getAndUpdate { current ->
            current.copy(models = models)
        }
    }

}

sealed interface ModelsEvent

data class ModelsState(
    val models: List<ModelUI> = emptyList(),
    val isLoading: Boolean = false,
    val addModelDialogState: AddModelDialogState? = null,
    val removeModelDialogState: RemoveModelDialogState? = null,
)

data class AddModelDialogState(
    val isAddEnabled: Boolean = false,
    val error: String? = null,
    val text: String = "",
)

data class RemoveModelDialogState(
    val model: String,
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