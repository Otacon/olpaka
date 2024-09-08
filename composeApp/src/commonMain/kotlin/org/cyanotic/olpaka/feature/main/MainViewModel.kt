package org.cyanotic.olpaka.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.ThemeState
import org.cyanotic.olpaka.repository.GenerateRepository

class MainViewModel(
    private val repository: GenerateRepository,
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

    fun onButtonClicked() = viewModelScope.launch(Dispatchers.Default) {
        repository.generate("Why is the sky blue?")
            .onStart { println("Generating...") }
            .onEach { println(it) }
            .onCompletion { println("Generated!") }
            .collect()
    }

}

data class MainState(
    val selectedTabIndex: Int = 0
)