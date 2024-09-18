package org.cyanotic.olpaka.feature.models

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.models_dialog_download_model_error_already_added
import org.cyanotic.olpaka.core.inBackground
import org.cyanotic.olpaka.repository.ModelsRepository
import org.jetbrains.compose.resources.getString

class ModelsAddModelViewModel(
    private val repository: ModelsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddModelState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddModelEvent>()
    val event = _events.asSharedFlow()

    private var models: List<String> = emptyList()

    fun onCreate() = inBackground {
        models = repository.getModels()
            .getOrDefault(emptyList())
            .map { it.id }
    }

    fun onModelNameChanged(text: String) = inBackground {
        val alreadyDownloaded = models.any { it == text.trim() }
        val isAddEnabled = text.isNotBlank() && !alreadyDownloaded
        val error = if (alreadyDownloaded) {
            getString(Res.string.models_dialog_download_model_error_already_added)
        } else {
            null
        }
        _state.value = AddModelState(
            okButtonEnabled = isAddEnabled,
            modelToAdd = text,
            error = error,
        )
    }

    fun onCancelClicked() = inBackground {
        _events.emit(AddModelEvent.Cancel)
    }

    fun onOkClicked() = inBackground {
        _events.emit(AddModelEvent.Confirm(_state.value.modelToAdd))
    }

}


sealed interface AddModelEvent {
    data object Cancel : AddModelEvent
    data class Confirm(val model: String) : AddModelEvent
}

data class AddModelState(
    val okButtonEnabled: Boolean = false,
    val modelToAdd: String = "",
    val error: String? = null
)