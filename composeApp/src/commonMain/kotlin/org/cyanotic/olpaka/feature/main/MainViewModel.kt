package org.cyanotic.olpaka.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.core.inBackground
import org.cyanotic.olpaka.repository.ModelsRepository

class MainViewModel(
    private val preferences: Preferences,
    private val modelsRepository: ModelsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>()
    val event = _events.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            modelsRepository.models
                .map { it.filterIsInstance<Model.Downloading>() }
                .collect { models ->
                    val current = _state.value
                    val isDownloading = models.isNotEmpty()
                    Napier.i(tag = "MainViewModel", message = "current = $current | isDownloading = $isDownloading")
                    val activityBadge = when {
                        current.selectedTabIndex == 1 -> Badge.NONE
                        !isDownloading -> {
                            when(current.activityBadge){
                                Badge.DOWNLOADING,
                                Badge.COMPLETED -> Badge.COMPLETED
                                Badge.NONE -> Badge.NONE
                            }
                        }
                        else -> Badge.DOWNLOADING
                    }
                    _state.value = current.copy(activityBadge = activityBadge)
                }
        }
    }

    fun onCreate() = inBackground {
        if (!preferences.hasSeenOnboarding) {
            _events.emit(MainEvent.OpenOnboarding)
            preferences.hasSeenOnboarding = true
        }
    }

    fun onTabChanged(index: Int) = inBackground {
        _state.getAndUpdate { current ->
            val navigationEvent = when (index) {
                1 -> MainEvent.OpenModels
                2 -> MainEvent.OpenSettings
                else -> MainEvent.OpenChat
            }
            val badge = when {
                index != 1 -> current.activityBadge
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