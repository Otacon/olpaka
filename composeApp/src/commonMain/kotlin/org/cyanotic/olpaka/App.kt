package org.cyanotic.olpaka

import androidx.compose.runtime.*
import org.cyanotic.olpaka.core.ThemeState
import org.cyanotic.olpaka.core.coreModule
import org.cyanotic.olpaka.feature.main.MainScreen
import org.cyanotic.olpaka.feature.main.mainModule
import org.cyanotic.olpaka.feature.models.modelsModule
import org.cyanotic.olpaka.feature.settings.settingsModule
import org.cyanotic.olpaka.network.networkModule
import org.cyanotic.olpaka.repository.repositoryModule
import org.cyanotic.olpaka.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.compose.getKoin

@Composable
@Preview
fun App() {
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
        modelsModule,
        settingsModule,
    )
}