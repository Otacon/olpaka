package org.cyanotic.olpaka.feature.main

import kotlinx.coroutines.flow.*
import org.cyanotic.olpaka.core.*

class MainViewModel(
    private val preferences: Preferences,
    private val modelDownloadState: ModelDownloadState,
) : OlpakaViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>()
    val event = _events.asSharedFlow()

    override fun onCreate() = inBackground {
        if (!preferences.hasSeenOnboarding) {
            _events.emit(MainEvent.OpenOnboarding)
            preferences.hasSeenOnboarding = true
        }
        inBackground {
            modelDownloadState.currentDownloadState.collect { newState ->
                val selectedTab = state.value.selectedTabIndex
                if (selectedTab != 1) {
                    _state.value = _state.value.copy(activityBadge = newState.toBadge())
                } else if(newState == DownloadState.COMPLETED){
                    modelDownloadState.setInactive()
                }
            }
        }
    }

    fun onTabChanged(index: Int) = inBackground {
        _state.getAndUpdate { current ->
            val navigationEvent = when (index) {
                1 -> MainEvent.OpenModels
                2 -> MainEvent.OpenSettings
                else -> MainEvent.OpenChat
            }
            val currentState = modelDownloadState.currentDownloadState.value
            val badge = when {
                index != 1 -> currentState.toBadge()
                currentState == DownloadState.COMPLETED -> {
                    modelDownloadState.setInactive()
                    Badge.NONE
                }

                else -> Badge.NONE
            }
            _events.emit(navigationEvent)
            current.copy(
                selectedTabIndex = index,
                activityBadge = badge
            )
        }
    }

}

private fun DownloadState.toBadge(): Badge {
    return when (this) {
        DownloadState.DOWNLOADING -> Badge.DOWNLOADING
        DownloadState.COMPLETED -> Badge.COMPLETED
        DownloadState.INACTIVE -> Badge.NONE
    }
}

data class MainState(
    val selectedTabIndex: Int = 0,
    val activityBadge: Badge = Badge.NONE
)

enum class Badge {
    DOWNLOADING,
    COMPLETED,
    NONE
}

sealed interface MainEvent {
    data object OpenChat : MainEvent
    data object OpenModels : MainEvent
    data object OpenSettings : MainEvent
    data object OpenOnboarding : MainEvent
}