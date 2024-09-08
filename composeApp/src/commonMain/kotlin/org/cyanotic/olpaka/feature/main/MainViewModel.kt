package org.cyanotic.olpaka.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.ThemeState

class MainViewModel(
    private val preferences: Preferences,
    private val themeState: ThemeState,
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    fun onCreate() {
        themeState.themeMode.value = preferences.themeMode
        themeState.color.value = preferences.themeColor
    }

    fun onTabChanged(index: Int) = viewModelScope.launch(Dispatchers.Default) {
        _state.getAndUpdate { current ->
            current.copy(selectedTabIndex = index)
        }
    }

}

data class MainState(
    val selectedTabIndex: Int = 0
)