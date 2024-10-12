package org.cyanotic.olpaka.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.repository.ModelsRepository

class MainViewModel(
    private val preferences: Preferences,
    private val modelsRepository: ModelsRepository,
    private val backgroundDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>()
    val event = _events.asSharedFlow()

    init {
        viewModelScope.launch(backgroundDispatcher) {
            modelsRepository.models.collect { models ->
                _state.getAndUpdate { current ->
                    val activityBadge = calculateBadgeState(
                        models = models,
                        tabIndex = current.selectedTabIndex,
                        currentBadge = current.activityBadge
                    )
                    current.copy(activityBadge = activityBadge)
                }
            }
        }
    }

    fun onCreate() = viewModelScope.launch(backgroundDispatcher) {
        if (!preferences.hasSeenOnboarding) {
            _events.emit(MainEvent.OpenOnboarding)
            preferences.hasSeenOnboarding = true
        }
    }

    fun onTabChanged(index: Int) = viewModelScope.launch(backgroundDispatcher) {
        _state.getAndUpdate { current ->
            val adjustedIndex = when {
                index <= 0 -> 0
                index >= 2 -> 2
                else -> index
            }

            val navigationEvent = when (adjustedIndex) {
                1 -> MainEvent.OpenModels
                2 -> MainEvent.OpenSettings
                else -> MainEvent.OpenChat
            }
            _events.emit(navigationEvent)

            val badge = calculateBadgeState(
                models = modelsRepository.models.value,
                tabIndex = adjustedIndex,
                currentBadge = current.activityBadge
            )
            current.copy(
                selectedTabIndex = adjustedIndex,
                activityBadge = badge
            )
        }
    }

    private fun calculateBadgeState(models: List<Model>, tabIndex: Int, currentBadge: Badge): Badge {
        if (tabIndex == 1) {
            return Badge.NONE
        }

        val isDownloading = models.filterIsInstance<Model.Downloading>().isNotEmpty()
        if (isDownloading) {
            return Badge.DOWNLOADING
        }

        return when (currentBadge) {
            Badge.DOWNLOADING,
            Badge.COMPLETED -> Badge.COMPLETED

            Badge.NONE -> Badge.NONE
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