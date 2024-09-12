package org.cyanotic.olpaka.core

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import org.cyanotic.olpaka.feature.about.AboutScreen
import org.cyanotic.olpaka.feature.chat.ChatScreen
import org.cyanotic.olpaka.feature.models.ModelsScreen
import org.cyanotic.olpaka.feature.onboarding.OnboardingScreen
import org.cyanotic.olpaka.feature.settings.SettingsScreen

object Routes {
    const val ABOUT = "About"
    const val CHAT = "Chat"
    const val MODELS = "Models"
    const val SETTINGS = "Settings"
    const val ONBOARDING = "Onboarding"
}

@Composable
fun OlpakaNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.CHAT,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Routes.CHAT) { ChatScreen() }
        composable(Routes.MODELS) { ModelsScreen() }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
        dialog(Routes.ONBOARDING) { OnboardingScreen(navController) }
        dialog(Routes.ABOUT) { AboutScreen(navController) }
    }
}