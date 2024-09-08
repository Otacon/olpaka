package org.cyanotic.olpaka.core

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.cyanotic.olpaka.feature.chat.ChatScreen
import org.cyanotic.olpaka.feature.models.ModelsScreen
import org.cyanotic.olpaka.feature.settings.SettingsScreen

object Routes {
    const val CHAT = "Chat"
    const val MODELS = "Models"
    const val SETTINGS = "Settings"
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
        composable(Routes.SETTINGS) { SettingsScreen() }
    }
}