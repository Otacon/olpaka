package org.cyanotic.olpaka.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.settings_connection_url_error
import org.cyanotic.olpaka.core.Analytics
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.ThemeState
import org.cyanotic.olpaka.core.inBackground
import org.cyanotic.olpaka.network.EndpointProvider
import org.cyanotic.olpaka.ui.theme.OlpakaColor
import org.cyanotic.olpaka.ui.theme.OlpakaTheme
import org.jetbrains.compose.resources.getString

class SettingsViewModel(
    private val themeState: ThemeState,
    private val preferences: Preferences,
    private val endpointProvider: EndpointProvider,
    private val analytics: Analytics,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    fun onCreate() {
        analytics.trackScreenView("settings")
        _state.getAndUpdate { current ->
            current.copy(
                selectedColor = preferences.themeColor,
                selectedTheme = preferences.themeMode,
                connectionHost = preferences.connectionHost,
            )
        }
    }

    fun onColorChanged(color: OlpakaColor) = inBackground {
        val analyticsName = when(color){
            OlpakaColor.OLPAKA -> "olpaka"
            OlpakaColor.RED -> "red"
            OlpakaColor.PURPLE -> "purple"
            OlpakaColor.BLUE -> "blue"
            OlpakaColor.ORANGE -> "orange"
            OlpakaColor.GREEN -> "green"
            OlpakaColor.GREY -> "grey"
        }
        analytics.trackEvent("change_theme_color", mapOf("color" to analyticsName))
        _state.getAndUpdate { current ->
            preferences.themeColor = color
            themeState.color.value = color
            current.copy(selectedColor = color)
        }
    }

    fun onThemeChanged(theme: OlpakaTheme) = inBackground {
        val analyticsName = when(theme){
            OlpakaTheme.AUTO -> "auto"
            OlpakaTheme.DARK -> "dark"
            OlpakaTheme.LIGHT -> "light"
        }
        analytics.trackEvent("change_theme_mode", mapOf("mode" to analyticsName))
        _state.getAndUpdate { current ->
            preferences.themeMode = theme
            themeState.themeMode.value = theme
            current.copy(selectedTheme = theme)
        }
    }

    fun onConnectionUrlChanged(newHost: String) = viewModelScope.launch {
        setBaseUrl(newHost)
    }

    fun revertDefaultConnectionUrl() = inBackground {
        setBaseUrl(EndpointProvider.DEFAULT_OLLAMA_API_URL)
    }

    private suspend fun setBaseUrl(newHost: String) {
        _state.getAndUpdate { current ->
            val url = parseUrl(newHost.trim())
            val hostError = if (url != null) {
                preferences.connectionHost = url.toString()
                endpointProvider.baseUrl = url
                null
            } else {
                getString(Res.string.settings_connection_url_error)
            }
            current.copy(connectionHost = newHost, hostError = hostError)
        }
    }

    fun onClearPreferencesClicked() {
        preferences.clear()
    }
}

data class SettingsState(
    val selectedColor: OlpakaColor = OlpakaColor.OLPAKA,
    val selectedTheme: OlpakaTheme = OlpakaTheme.AUTO,
    val connectionHost: String = "",
    val hostError: String? = null,
)
