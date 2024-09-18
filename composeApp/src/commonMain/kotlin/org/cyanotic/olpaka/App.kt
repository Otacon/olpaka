package org.cyanotic.olpaka

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.cyanotic.olpaka.BuildKonfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import io.ktor.http.*
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.ThemeState
import org.cyanotic.olpaka.feature.main.MainScreen
import org.cyanotic.olpaka.network.EndpointProvider
import org.cyanotic.olpaka.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.getKoin

@Composable
@Preview
fun App() {

    val logLevel = when (BuildKonfig.loggingLevel.lowercase()) {
        "info" -> LogLevel.INFO
        "debug" -> LogLevel.DEBUG
        "warning" -> LogLevel.WARNING
        "error" -> LogLevel.ERROR
        "verbose" -> LogLevel.VERBOSE
        else -> {
            print("Warning: Unknown logging level. Disabling it.")
            null
        }
    }
    logLevel?.let {
        Napier.base(DebugAntilog())
        Napier.isEnable(it, null)
    }

    KoinApplication({ modules(koinModules) }) {
        val koin = getKoin()
        val themeState = koin.get<ThemeState>()
        val preferences = koin.get<Preferences>()
        val endpointProvider = koin.get<EndpointProvider>()
        themeState.themeMode.value = preferences.themeMode
        themeState.color.value = preferences.themeColor
        parseUrl(preferences.connectionHost)?.let { endpointProvider.baseUrl = it }

        val isDark = themeState.themeMode.collectAsState()
        val seedColor = themeState.color.collectAsState()
        AppTheme(
            seedColor.value,
            isDark.value
        ) {
            MainScreen()
        }
    }
}