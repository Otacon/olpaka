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
        _state.getAndUpdate { current ->
            when (current) {
                ModelsState.Error,
                ModelsState.Loading -> current

                is ModelsState.Content -> current.copy(addModelDialogState = AddModelDialogState())
            }
        }
    }

    fun onAddModelTextChanged(text: String) = inBackground {
        val current = _state.value
        when (current) {
            is ModelsState.Content -> Unit
            ModelsState.Error,
            ModelsState.Loading -> return@inBackground
        }
        val dialogState = current.addModelDialogState ?: return@inBackground
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
        _state.value = current.copy(addModelDialogState = newDialogState)
    }

    fun onCloseAddModelDialog() {
        _state.getAndUpdate { current ->
            when (current) {
                is ModelsState.Content -> current.copy(addModelDialogState = null)
                ModelsState.Error,
                ModelsState.Loading -> current
            }

        }
    }

    fun onAddModel(tag: String) = inBackground {
        val currentState = when (val current = _state.value) {
            is ModelsState.Content -> current.copy(isLoading = true, addModelDialogState = null)
            ModelsState.Error,
            ModelsState.Loading -> ModelsState.Content(isLoading = true, addModelDialogState = null)
        }
        repository.downloadModel(tag)
            .onStart {
                cancelDownload = false
                val newModels = currentState.models + ModelUI.Downloading(
                    key = tag,
                    title = tag,
                    subtitle = getString(Res.string.models_state_initializing),
                    progress = null
                )
                _state.value = currentState.copy(models = newModels)
                modelDownloadState.setDownloading()
            }
            .onCompletion {
                viewModelScope.launch {
                    refreshModels()
                    modelDownloadState.setCompleted()
                }
            }
            .collect { chunk ->
                if (cancelDownload) {
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
                _state.value = ModelsState.Content(models = newModels, isLoading = false)
            }
    }

    fun onCancelDownload() {
        cancelDownload = true
    }

    fun onRemoveModelClicked(model: ModelUI.Available) = inBackground {
        _state.getAndUpdate { current ->
            when (current) {
                is ModelsState.Content -> current.copy(removeModelDialogState = RemoveModelDialogState(model.key))
                ModelsState.Error,
                ModelsState.Loading -> current
            }
        }
    }

    fun onRemoveModelDialogResult(remove: Boolean) = inBackground {
        val currentState = when (val current = _state.value) {
            is ModelsState.Content -> current.copy(removeModelDialogState = null)
            ModelsState.Error,
            ModelsState.Loading -> return@inBackground
        }
        val model = currentState.removeModelDialogState?.model
        _state.value = currentState
        if (!remove || model == null) {
            return@inBackground
        }
        _state.value = currentState.copy(isLoading = true)
        repository.removeModel(tag = model)
        refreshModels()
        _state.value = currentState.copy(isLoading = false)
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

sealed interface ModelsEvent

sealed interface ModelsState {

    data class Content(
        val models: List<ModelUI> = emptyList(),
        val isLoading: Boolean = false,
        val addModelDialogState: AddModelDialogState? = null,
        val removeModelDialogState: RemoveModelDialogState? = null,
    ) : ModelsState

    data object Loading : ModelsState

    data object Error : ModelsState
}


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