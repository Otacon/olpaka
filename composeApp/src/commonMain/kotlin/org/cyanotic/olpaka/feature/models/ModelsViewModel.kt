package org.cyanotic.olpaka.feature.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.error_missing_ollama_message
import olpaka.composeapp.generated.resources.error_missing_ollama_title
import org.cyanotic.olpaka.core.FirebaseAnalytics
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.repository.ModelsRepository
import org.jetbrains.compose.resources.getString

class ModelsViewModel(
    private val repository: ModelsRepository,
    private val analytics: FirebaseAnalytics,
    private val statsFormatter: DownloadStatsFormatter,
) : ViewModel() {

    private val _state = MutableStateFlow<ModelsState>(ModelsState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ModelsEvent>()
    val event = _events.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            repository.models.collect { models ->
                val isDownloadingModel = models.filterIsInstance<Model.Downloading>().isNotEmpty()
                _state.value = ModelsState.Content(
                    models = models.map { it.toModelUI() },
                    controlsEnabled = !isDownloadingModel
                )
            }
        }
    }


    fun onCreate() = viewModelScope.launch(Dispatchers.Default) {
        analytics.screenView("models")
        refreshModels()
    }

    fun onRefreshClicked() = viewModelScope.launch(Dispatchers.Default) {
        analytics.event("refresh_models")
        refreshModels()
    }

    fun onAddModelClicked() = viewModelScope.launch(Dispatchers.Default) {
        _events.emit(ModelsEvent.OpenAddModelDialog)
    }

    fun onAddModel(tag: String) = viewModelScope.launch(Dispatchers.Default) {
        repository.downloadModel(tag)
    }

    fun onCancelDownload() {
        repository.cancelDownload()
    }

    fun onRemoveModelClicked(model: ModelUI.Available) =
        viewModelScope.launch(Dispatchers.Default) {
            _events.emit(ModelsEvent.OpenRemoveModelDialog(model.key))
        }

    fun onConfirmRemoveModel(modelKey: String) = viewModelScope.launch(Dispatchers.Default) {
        _state.value = ModelsState.Content(
            models = repository.models.value.map { it.toModelUI() },
            controlsEnabled = false
        )
        repository.removeModel(tag = modelKey)
        analytics.event("remove_model", properties = mapOf("model" to modelKey))
        refreshModels()
    }

    private suspend fun refreshModels() {
        val result = repository.refreshModels()
        if (result.isFailure) {
            _state.value = ModelsState.Error(
                title = getString(Res.string.error_missing_ollama_title),
                message = getString(Res.string.error_missing_ollama_message),
            )
        }
    }

    private fun Model.toModelUI(): ModelUI {
        return when (this) {
            is Model.Cached -> {
                val subtitle = listOfNotNull(
                    statsFormatter.formatSizeInBytes(size),
                    quantization,
                    parameters
                ).joinToString(" • ")
                ModelUI.Available(
                    key = id,
                    title = name,
                    subtitle = subtitle
                )
            }

            is Model.Downloading -> {
                var progress: Float? = null
                var subtitle = "Downloading..."
                if (sizeBytes > 0) {
                    progress = downloadedBytes / sizeBytes.toFloat()
                    val speedFormatted = statsFormatter.formatDownloadSpeed(this.speedBytesSecond)
                    val downloadedSizeFormatted =
                        statsFormatter.formatSizeInBytes(this.downloadedBytes)
                    val totalSizeFormatted = statsFormatter.formatSizeInBytes(this.sizeBytes)
                    val timeLeftFormatted = statsFormatter.formatRemainingTime(this.timeLeftSeconds)
                    subtitle =
                        "$speedFormatted - $downloadedSizeFormatted of $totalSizeFormatted, $timeLeftFormatted left"
                }
                ModelUI.Downloading(
                    key = id,
                    title = name,
                    subtitle = subtitle,
                    progress = progress
                )
            }

            is Model.Error -> {
                ModelUI.Error(
                    key = id,
                    title = name,
                    subtitle = this.message,
                )
            }
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

