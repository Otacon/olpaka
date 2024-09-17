package org.cyanotic.olpaka.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assistant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.home_tab_name_chat
import olpaka.composeapp.generated.resources.home_tab_name_models
import olpaka.composeapp.generated.resources.home_tab_name_settings
import org.cyanotic.olpaka.core.OlpakaNavHost
import org.cyanotic.olpaka.core.Routes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen() {
    val viewModel = koinViewModel<MainViewModel>().also { it.init() }
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()
    val chatSelected = state.selectedTabIndex == 0
    val modelsSelected = state.selectedTabIndex == 1
    val settingsSelected = state.selectedTabIndex == 2
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
                    label = { Text(stringResource(Res.string.home_tab_name_chat)) },
                    icon = {
                        val icon = if (chatSelected) {
                            Icons.AutoMirrored.Filled.Chat
                        } else {
                            Icons.AutoMirrored.Outlined.Chat
                        }
                        Icon(icon, contentDescription = "")
                    },
                    selected = chatSelected,
                    onClick = { viewModel.onTabChanged(0) }
                )
                NavigationRailItem(
                    label = { Text(stringResource(Res.string.home_tab_name_models)) },
                    icon = {
                        Box {
                            val icon = if (modelsSelected) {
                                Icons.Filled.Assistant
                            } else {
                                Icons.Outlined.Assistant
                            }
                            Icon(icon, contentDescription = "")
                            when (state.activityBadge) {
                                Badge.DOWNLOADING -> Badge(
                                    contentColor = LOADING_COLOR,
                                    containerColor = LOADING_COLOR
                                )

                                Badge.COMPLETED -> Badge(
                                    contentColor = SUCCESS_COLOR,
                                    containerColor = SUCCESS_COLOR
                                )

                                Badge.NONE -> Unit
                            }

                        }
                    },
                    selected = modelsSelected,
                    onClick = { viewModel.onTabChanged(1) }
                )
                NavigationRailItem(
                    label = { Text(stringResource(Res.string.home_tab_name_settings)) },
                    icon = {
                        val icon = if (settingsSelected) {
                            Icons.Filled.Settings
                        } else {
                            Icons.Outlined.Settings
                        }
                        Icon(icon, contentDescription = "")
                    },
                    selected = settingsSelected,
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
                popUpTo(navController.graph.startDestinationRoute!!) {
                    saveState = true
                }
            }
        }

        MainEvent.OpenModels -> {
            navController.navigate(Routes.MODELS) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.startDestinationRoute!!) {
                    saveState = true
                }
            }
        }

        MainEvent.OpenSettings -> {
            navController.navigate(Routes.SETTINGS) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.startDestinationRoute!!) {
                    saveState = true
                }
            }
        }

        MainEvent.OpenOnboarding -> {
            navController.navigate(Routes.ONBOARDING) {
                popUpTo(navController.graph.startDestinationRoute!!)
            }
        }
    }
}

private val LOADING_COLOR = Color(0xFFFF9800)
private val SUCCESS_COLOR = Color(0xFF4CAF50)
