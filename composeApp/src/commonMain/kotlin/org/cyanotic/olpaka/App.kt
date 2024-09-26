package org.cyanotic.olpaka

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.cyanotic.olpaka.BuildKonfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import io.ktor.http.*
import org.cyanotic.olpaka.core.Analytics
import org.cyanotic.olpaka.core.GoogleAnalytics
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
        LaunchedEffect(Unit){
            val preferences = koin.get<Preferences>()
            val analytics = koin.get<GoogleAnalytics>()
            initAnalytics(preferences, analytics)


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
    logLevel?.let {
        Napier.base(DebugAntilog())
        Napier.isEnable(it, null)
    }
}

private fun initAnalytics(preferences: Preferences, analytics: Analytics) {
    val measurementId = BuildKonfig.analyticsMeasurementId
    val apiSecret = BuildKonfig.analyticsApiSecret
    Napier.d(tag = "Init") { "Initialising analytics with \"measurementId\": $measurementId, \"apiSecret\": $apiSecret." }
    if (measurementId.isNotBlank() && apiSecret.isNotBlank()) {
        analytics.init(
            clientId = preferences.analyticsClientId,
            measurementId = measurementId,
            apiSecret = apiSecret,
            debug = true,
        )
    } else {
        Napier.w(tag = "Init") { "Analytics are not being initialised as measurementId or apiSecrets are invalid." }
    }
}

private fun initTheme(preferences: Preferences, themeState: ThemeState) {
    themeState.themeMode.value = preferences.themeMode
    themeState.color.value = preferences.themeColor
}

private fun initHttpClient(preferences: Preferences, endpointProvider: EndpointProvider) {
    parseUrl(preferences.connectionHost)?.let { endpointProvider.baseUrl = it }
}