package org.cyanotic.olpaka.core

import org.cyanotic.olpaka.ui.theme.OlpakaColor
import org.cyanotic.olpaka.ui.theme.OlpakaTheme
import com.russhwolf.settings.Settings

class Preferences {
    private val settings = Settings()

    var themeMode: OlpakaTheme
        set(value) {
            val mode = when (value) {
                OlpakaTheme.AUTO -> VALUE_THEME_AUTO
                OlpakaTheme.LIGHT -> VALUE_THEME_LIGHT
                OlpakaTheme.DARK -> VALUE_THEME_DARK
            }
            settings.putString(KEY_THEME_MODE, mode)
        }
        get() {
            return when (settings.getStringOrNull(KEY_THEME_MODE)) {
                VALUE_THEME_LIGHT -> OlpakaTheme.LIGHT
                VALUE_THEME_DARK -> OlpakaTheme.DARK
                else -> OlpakaTheme.AUTO
            }
        }

    var themeColor: OlpakaColor
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

    fun clear() {
        settings.clear()
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_THEME_COLOR = "theme_color"

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