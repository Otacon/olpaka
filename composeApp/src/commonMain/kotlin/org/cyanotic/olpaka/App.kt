package org.cyanotic.olpaka

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.cyanotic.olpaka.BuildKonfig
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import io.ktor.http.parseUrl
import org.cyanotic.olpaka.core.FirebaseAnalytics
import org.cyanotic.olpaka.core.OlpakaAntilog
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
    initLogging()
    KoinApplication({ modules(koinModules) }) {
        val koin = getKoin()
        val themeState = koin.get<ThemeState>()
        LaunchedEffect(Unit) {
            val analytics = koin.get<FirebaseAnalytics>()
            analytics.init()

            val preferences = koin.get<Preferences>()
            initTheme(preferences, themeState)

            val endpointProvider = koin.get<EndpointProvider>()
            initHttpClient(preferences, endpointProvider)
        }


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

private fun initLogging() {
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
    val antilog = OlpakaAntilog()
    logLevel?.let { antilog.currentLevel = it }
    Napier.base(antilog)
}

private fun initTheme(preferences: Preferences, themeState: ThemeState) {
    themeState.themeMode.value = preferences.themeMode
    themeState.color.value = preferences.themeColor
}

private fun initHttpClient(preferences: Preferences, endpointProvider: EndpointProvider) {
    parseUrl(preferences.connectionHost)?.let { endpointProvider.baseUrl = it }
}