package org.cyanotic.olpaka.core

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import org.cyanotic.olpaka.network.EndpointProvider.Companion.DEFAULT_OLLAMA_API_URL
import org.cyanotic.olpaka.ui.theme.OlpakaColor
import org.cyanotic.olpaka.ui.theme.OlpakaTheme
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface Preferences {

    val analyticsClientId: String
    var hasSeenOnboarding: Boolean
    var lastUsedModel: String?
    var themeMode: OlpakaTheme
    var connectionHost: String
    var themeColor: OlpakaColor

    fun clear()
}

class PreferencesDefault : Preferences {
    private val settings = Settings()

    @OptIn(ExperimentalUuidApi::class)
    override val analyticsClientId: String
        get() {
            val clientId = settings.getStringOrNull(KEY_ANALYTICS_IDENTIFIER)
            return if (clientId == null) {
                val uuid = Uuid.random().toString()
                settings[KEY_ANALYTICS_IDENTIFIER] = uuid
                uuid
            } else {
                clientId
            }
        }

    override var hasSeenOnboarding: Boolean
        set(value) {
            settings[KEY_SEEN_ONBOARDING] = value
        }
        get() {
            return settings[KEY_SEEN_ONBOARDING] ?: false
        }

    override var lastUsedModel: String?
        set(value) {
            settings[KEY_LAST_USED_MODEL] = value

        }
        get() {
            return settings[KEY_LAST_USED_MODEL]
        }

    override var themeMode: OlpakaTheme
        set(value) {
            val mode = when (value) {
                OlpakaTheme.AUTO -> VALUE_THEME_AUTO
                OlpakaTheme.LIGHT -> VALUE_THEME_LIGHT
                OlpakaTheme.DARK -> VALUE_THEME_DARK
            }
            settings[KEY_THEME_MODE] = mode
        }
        get() {
            return when (settings.getStringOrNull(KEY_THEME_MODE)) {
                VALUE_THEME_LIGHT -> OlpakaTheme.LIGHT
                VALUE_THEME_DARK -> OlpakaTheme.DARK
                else -> OlpakaTheme.AUTO
            }
        }

    override var connectionHost: String
        set(value) {
            settings[KEY_CONNECTION_HOST] = value
        }
        get() {
            return settings.getString(KEY_CONNECTION_HOST, DEFAULT_OLLAMA_API_URL)
        }

    override var themeColor: OlpakaColor
        set(value) {
            val mode = when (value) {
                OlpakaColor.OLPAKA -> VALUE_THEME_COLOR_OLPAKA
                OlpakaColor.RED -> VALUE_THEME_COLOR_RED
                OlpakaColor.PURPLE -> VALUE_THEME_COLOR_PURPLE
                OlpakaColor.BLUE -> VALUE_THEME_COLOR_BLUE
                OlpakaColor.ORANGE -> VALUE_THEME_COLOR_ORANGE
                OlpakaColor.GREEN -> VALUE_THEME_COLOR_GREEN
                OlpakaColor.GREY -> VALUE_THEME_COLOR_GREY
            }
            settings.putString(KEY_THEME_COLOR, mode)
        }
        get() {
            return when (settings.getStringOrNull(KEY_THEME_COLOR)) {
                VALUE_THEME_COLOR_RED -> OlpakaColor.RED
                VALUE_THEME_COLOR_PURPLE -> OlpakaColor.PURPLE
                VALUE_THEME_COLOR_BLUE -> OlpakaColor.BLUE
                VALUE_THEME_COLOR_ORANGE -> OlpakaColor.ORANGE
                VALUE_THEME_COLOR_GREEN -> OlpakaColor.GREEN
                VALUE_THEME_COLOR_GREY -> OlpakaColor.GREY
                else -> OlpakaColor.OLPAKA
            }
        }

    override fun clear() {
        settings.clear()
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_THEME_COLOR = "theme_color"
        private const val KEY_SEEN_ONBOARDING = "seen_onboarding"
        private const val KEY_LAST_USED_MODEL = "last_used_model"

        private const val KEY_CONNECTION_HOST = "connection_host"
        private const val KEY_ANALYTICS_IDENTIFIER = "analytics_identifier"

        private const val VALUE_THEME_AUTO = "auto"
        private const val VALUE_THEME_LIGHT = "light"
        private const val VALUE_THEME_DARK = "dark"

        private const val VALUE_THEME_COLOR_OLPAKA = "olpaka"
        private const val VALUE_THEME_COLOR_RED = "red"
        private const val VALUE_THEME_COLOR_PURPLE = "purple"
        private const val VALUE_THEME_COLOR_BLUE = "blue"
        private const val VALUE_THEME_COLOR_ORANGE = "orange"
        private const val VALUE_THEME_COLOR_GREEN = "green"
        private const val VALUE_THEME_COLOR_GREY = "grey"
    }
}