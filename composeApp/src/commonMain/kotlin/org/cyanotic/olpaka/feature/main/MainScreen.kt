package org.cyanotic.olpaka.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Assistant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cyanotic.olpaka.feature.chat.ChatScreen
import org.cyanotic.olpaka.feature.models.ModelsScreen
import org.cyanotic.olpaka.feature.settings.SettingsScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun MainScreen() {
    val viewModel = koinViewModel<MainViewModel>().also { it.onCreate() }
    val state by viewModel.state.collectAsState()
    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
            ,
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
        Box(Modifier.fillMaxSize()) {
            when(state.selectedTabIndex){
                1 -> ModelsScreen()
                2 -> SettingsScreen()
                else -> ChatScreen()
            }
        }
    }
}
