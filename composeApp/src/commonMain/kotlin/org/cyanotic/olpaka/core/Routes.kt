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
import org.cyanotic.olpaka.feature.models.ModelsAddModelScreen
import org.cyanotic.olpaka.feature.models.ModelsRemoveModelScreen
import org.cyanotic.olpaka.feature.models.ModelsScreen
import org.cyanotic.olpaka.feature.onboarding.OnboardingScreen
import org.cyanotic.olpaka.feature.settings.SettingsScreen

object Routes {
    const val ABOUT = "about/"
    const val CHAT = "chat/"
    const val MODELS = "models/"
    const val MODELS_REMOVE_MODEL_DIALOG = "models/remove_model_dialog/"
    const val MODELS_ADD_MODEL_DIALOG = "models/add_model_dialog/"
    const val SETTINGS = "settings/"
    const val ONBOARDING = "onboarding/"
}

object Results{
    const val RESULT_REMOVE_MODEL_KEY = "removeModel"
    const val RESULT_ADD_MODEL_KEY = "addModel"
}

@Composable
fun OlpakaNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.CHAT,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Routes.CHAT) { ChatScreen() }
        composable(Routes.MODELS) { ModelsScreen(navController) }
        dialog(Routes.MODELS_REMOVE_MODEL_DIALOG + "{modelKey}") { backStackEntry ->
            val modelName = backStackEntry.arguments?.getString("modelKey")
            ModelsRemoveModelScreen(navController, modelName = modelName!!)
        }
        dialog(Routes.MODELS_ADD_MODEL_DIALOG) {
            ModelsAddModelScreen(navController)
        }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
        dialog(Routes.ONBOARDING) { OnboardingScreen(navController) }
        dialog(Routes.ABOUT) { AboutScreen(navController) }
    }
}