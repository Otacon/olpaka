package org.cyanotic.olpaka.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Assistant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.cyanotic.olpaka.core.OlpakaNavHost
import org.cyanotic.olpaka.core.Routes
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen() {
    val viewModel = koinViewModel<MainViewModel>().also { it.init() }
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.event.collect { handleEvent(it, navController) }
    }
    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            NavigationRail(
                modifier = Modifier.weight(1f)
            ) {
                NavigationRailItem(
                    label = { Text("Chat") },
                    icon = { Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = "") },
                    selected = state.selectedTabIndex == 0,
                    onClick = { viewModel.onTabChanged(0) }
                )
                NavigationRailItem(
                    label = { Text("Models") },
                    icon = { Icon(Icons.Outlined.Assistant, contentDescription = "") },
                    selected = state.selectedTabIndex == 1,
                    onClick = { viewModel.onTabChanged(1) }
                )
                NavigationRailItem(
                    label = { Text("Settings") },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "") },
                    selected = state.selectedTabIndex == 2,
                    onClick = { viewModel.onTabChanged(2) }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        VerticalDivider(Modifier.width(1.dp))
        OlpakaNavHost(navController)
    }
}

private fun handleEvent(event: MainEvent, navController: NavController) {
    when (event) {
        MainEvent.OpenChat -> {
            navController.navigate(Routes.CHAT) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
            }
        }

        MainEvent.OpenModels -> {
            navController.navigate(Routes.MODELS) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
            }
        }

        MainEvent.OpenSettings -> {
            navController.navigate(Routes.SETTINGS) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
            }
        }

        MainEvent.OpenOnboarding -> {
            navController.navigate(Routes.ONBOARDING) {
                popUpTo(navController.graph.startDestinationId)
            }
        }
    }
}
