package org.cyanotic.olpaka

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.cyanotic.olpaka.BuildKonfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import org.cyanotic.olpaka.core.ThemeState
import org.cyanotic.olpaka.core.coreModule
import org.cyanotic.olpaka.feature.chat.chatModule
import org.cyanotic.olpaka.feature.main.MainScreen
import org.cyanotic.olpaka.feature.main.mainModule
import org.cyanotic.olpaka.feature.models.modelsModule
import org.cyanotic.olpaka.feature.onboarding.onboardingModule
import org.cyanotic.olpaka.feature.settings.settingsModule
import org.cyanotic.olpaka.network.networkModule
import org.cyanotic.olpaka.repository.repositoryModule
import org.cyanotic.olpaka.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.getKoin
import org.koin.dsl.koinApplication

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

    KoinApplication(::koinConfiguration) {
        val themeState = getKoin().get<ThemeState>()
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

private fun koinConfiguration() = koinApplication {
    modules(
        coreModule,
        networkModule,
        repositoryModule,
        mainModule,
        onboardingModule,
        chatModule,
        modelsModule,
        settingsModule,
    )
}