package org.cyanotic.olpaka.feature.onboarding

import kotlinx.coroutines.flow.*
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.onboarding_cta_finish
import olpaka.composeapp.generated.resources.onboarding_cta_next
import org.cyanotic.olpaka.core.OlpakaViewModel
import org.jetbrains.compose.resources.getString

class OnboardingViewModel : OlpakaViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val event = _events.asSharedFlow()

    override fun onCreate() = inBackground {
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

    }

    private suspend fun updateStateForStep(stepNumber: Int) {
        if(stepNumber > 2){
            _events.emit(OnboardingEvent.Close)
            return
        }
        val fixedStepNumber = if(stepNumber < 0){
            0
        } else {
            stepNumber
        }
        val isPreviousVisible = fixedStepNumber != 0
        val nextText = if(fixedStepNumber <= 1){
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
    val isConnectionSuccessful: Boolean = false,
)

sealed interface OnboardingEvent {
    data object Close : OnboardingEvent
    data class OpenBrowser(
        val url: String
    ) : OnboardingEvent
}