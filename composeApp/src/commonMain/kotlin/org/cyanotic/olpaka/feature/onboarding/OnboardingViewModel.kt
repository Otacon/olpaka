package org.cyanotic.olpaka.feature.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.onboarding_cta_finish
import olpaka.composeapp.generated.resources.onboarding_cta_next
import org.cyanotic.olpaka.core.inBackground
import org.cyanotic.olpaka.repository.ModelsRepository
import org.jetbrains.compose.resources.getString

class OnboardingViewModel(
    private val repository: ModelsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val event = _events.asSharedFlow()

    fun onCreate() = inBackground {
        _state.value = OnboardingState(
            currentStep = 0,
            isPreviousVisible = false,
            nextText = getString(Res.string.onboarding_cta_next)
        )
    }

    fun onNextPressed() = inBackground {
        updateStateForStep(_state.value.currentStep + 1)
    }

    fun onPreviousPressed() = inBackground {
        updateStateForStep(_state.value.currentStep - 1)
    }

    fun onDownloadOllamaClicked() = inBackground {
        _events.emit(OnboardingEvent.OpenBrowser("https://ollama.com/download"))
    }

    fun onSetupCorsClicked() = inBackground {
        _events.emit(OnboardingEvent.OpenBrowser("https://github.com/Otacon/olpaka/blob/main/docs/setup_cors.md"))
    }

    fun onCheckConnectionClicked() = inBackground {
        val connectionState = if(repository.getModels().isSuccess) {
            ConnectionCheckState.SUCCESS
        } else {
            ConnectionCheckState.FAILURE
        }
        _state.value = _state.value.copy(connectionState = connectionState)
    }

    private suspend fun updateStateForStep(stepNumber: Int) {
        if (stepNumber > 2) {
            _events.emit(OnboardingEvent.Close)
            return
        }
        val fixedStepNumber = if (stepNumber < 0) {
            0
        } else {
            stepNumber
        }
        val isPreviousVisible = fixedStepNumber != 0
        val nextText = if (fixedStepNumber <= 1) {
            getString(Res.string.onboarding_cta_next)
        } else {
            getString(Res.string.onboarding_cta_finish)
        }
        _state.value = state.value.copy(
            currentStep = fixedStepNumber,
            nextText = nextText,
            isPreviousVisible = isPreviousVisible
        )
    }

}

data class OnboardingState(
    val currentStep: Int = 0,
    val isPreviousVisible: Boolean = false,
    val nextText: String = "",
    val connectionState: ConnectionCheckState = ConnectionCheckState.UNKNOWN,
)

sealed interface OnboardingEvent {
    data object Close : OnboardingEvent
    data class OpenBrowser(
        val url: String
    ) : OnboardingEvent
}

enum class ConnectionCheckState {
    UNKNOWN,
    SUCCESS,
    FAILURE
}