package org.cyanotic.olpaka.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.ThemeState

class MainViewModel(
    private val preferences: Preferences,
    private val themeState: ThemeState,
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>()
    val event = _events.asSharedFlow()

    fun onCreate() {
        themeState.themeMode.value = preferences.themeMode
        themeState.color.value = preferences.themeColor
    }

    fun onTabChanged(index: Int) = viewModelScope.launch(Dispatchers.Default) {
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
}