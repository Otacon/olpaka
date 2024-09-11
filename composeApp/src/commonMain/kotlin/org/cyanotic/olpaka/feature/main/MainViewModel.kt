package org.cyanotic.olpaka.feature.main

import kotlinx.coroutines.flow.*
import org.cyanotic.olpaka.core.OlpakaViewModel
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.ThemeState

class MainViewModel(
    private val preferences: Preferences,
    private val themeState: ThemeState,
) : OlpakaViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>()
    val event = _events.asSharedFlow()

    override fun onCreate() = inBackground {
        themeState.themeMode.value = preferences.themeMode
        themeState.color.value = preferences.themeColor
        _events.emit(MainEvent.OpenOnboarding)
    }

    fun onTabChanged(index: Int) = inBackground {
        _state.getAndUpdate { current ->
            val navigationEvent = when (index) {
                1 -> MainEvent.OpenModels
                2 -> MainEvent.OpenSettings
                else -> MainEvent.OpenChat
            }
            _events.emit(navigationEvent)
            current.copy(selectedTabIndex = index)
        }
    }

}

data class MainState(
    val selectedTabIndex: Int = 0
)

sealed interface MainEvent {
    data object OpenChat : MainEvent
    data object OpenModels : MainEvent
    data object OpenSettings : MainEvent
    data object OpenOnboarding : MainEvent
}