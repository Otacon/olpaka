package org.cyanotic.olpaka.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.ThemeState
import org.cyanotic.olpaka.ui.theme.OlpakaColor
import org.cyanotic.olpaka.ui.theme.OlpakaTheme

class SettingsViewModel(
    private val themeState: ThemeState,
    private val preferences: Preferences,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    fun onCreate() {
        _state.getAndUpdate { current ->
            current.copy(
                selectedColor = preferences.themeColor,
                selectedTheme = preferences.themeMode
            )
        }
    }

    fun onColorChanged(color: OlpakaColor) = viewModelScope.launch(Dispatchers.Default) {
        _state.getAndUpdate { current ->
            preferences.themeColor = color
            themeState.color.value = color
            current.copy(selectedColor = color)
        }
    }

    fun onThemeChanged(theme: OlpakaTheme) = viewModelScope.launch(Dispatchers.Default) {
        _state.getAndUpdate { current ->
            preferences.themeMode = theme
            themeState.themeMode.value = theme
            current.copy(selectedTheme = theme)
        }
    }
}

data class SettingsState(
    val selectedColor: OlpakaColor = OlpakaColor.OLPAKA,
    val selectedTheme: OlpakaTheme = OlpakaTheme.AUTO
)
