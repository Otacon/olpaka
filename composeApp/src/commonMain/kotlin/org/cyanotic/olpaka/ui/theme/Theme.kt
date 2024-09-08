package org.cyanotic.olpaka.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme


@Composable
fun AppTheme(
    color: OlpakaColor = OlpakaColor.OLPAKA,
    theme: OlpakaTheme = OlpakaTheme.DARK,
    content: @Composable () -> Unit
) {
    val seedColor = when(color){
        OlpakaColor.OLPAKA -> olpakaColorOlpaka
        OlpakaColor.RED ->    olpakaColorRed
        OlpakaColor.PURPLE -> olpakaColorPurple
        OlpakaColor.BLUE ->   olpakaColorBlue
        OlpakaColor.ORANGE -> olpakaColorOrange
        OlpakaColor.GREEN ->  olpakaColorGreen
        OlpakaColor.GREY ->   olpakaColorGrey
    }
    val isDarkMode = when(theme){
        OlpakaTheme.AUTO -> isSystemInDarkTheme()
        OlpakaTheme.LIGHT -> false
        OlpakaTheme.DARK -> true
    }
    val colorScheme = rememberDynamicColorScheme(seedColor, isDarkMode, style = PaletteStyle.TonalSpot)

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}


enum class OlpakaTheme {
    AUTO,
    LIGHT,
    DARK
}

enum class OlpakaColor {
    OLPAKA,
    RED,
    PURPLE,
    BLUE,
    ORANGE,
    GREEN,
    GREY
}

val olpakaColorOlpaka = Color(0xFFF21368)
val olpakaColorRed = Color(0xFFF44336)
val olpakaColorPurple = Color(0xFF9C27B0)
val olpakaColorBlue = Color(0xFF2196F3)
val olpakaColorOrange = Color(0xFFFF9800)
val olpakaColorGreen = Color(0xFF4CAF50)
val olpakaColorGrey = Color(0xFF9E9E9E)

