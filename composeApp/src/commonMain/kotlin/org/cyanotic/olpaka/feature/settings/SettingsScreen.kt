package org.cyanotic.olpaka.feature.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.cyanotic.olpaka.core.Routes
import org.cyanotic.olpaka.ui.OlpakaAppBar
import org.cyanotic.olpaka.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(navHostController: NavHostController) {
    val viewModel = koinViewModel<SettingsViewModel>().also { it.init() }
    val state by viewModel.state.collectAsState()
    Scaffold(topBar = { OlpakaAppBar("Settings") }) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SettingSection("Theme")
            SettingItem("Mode") {
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        icon = {
                            Icon(
                                if (state.selectedTheme == OlpakaTheme.AUTO) Icons.Filled.Done else Icons.Outlined.BrightnessAuto,
                                contentDescription = null
                            )
                        },
                        label = { Text("Auto") },
                        selected = state.selectedTheme == OlpakaTheme.AUTO,
                        shape = SegmentedButtonDefaults.itemShape(0, 3),
                        onClick = { viewModel.onThemeChanged(OlpakaTheme.AUTO) },
                    )
                    SegmentedButton(
                        icon = {
                            Icon(
                                if (state.selectedTheme == OlpakaTheme.DARK) Icons.Filled.Done else Icons.Outlined.DarkMode,
                                contentDescription = null
                            )
                        },
                        label = { Text("Dark") },
                        selected = state.selectedTheme == OlpakaTheme.DARK,
                        shape = SegmentedButtonDefaults.itemShape(1, 3),
                        onClick = { viewModel.onThemeChanged(OlpakaTheme.DARK) },
                    )
                    SegmentedButton(
                        icon = {
                            Icon(
                                if (state.selectedTheme == OlpakaTheme.LIGHT) Icons.Filled.Done else Icons.Outlined.LightMode,
                                contentDescription = null
                            )
                        },
                        label = { Text("Light") },
                        selected = state.selectedTheme == OlpakaTheme.LIGHT,
                        shape = SegmentedButtonDefaults.itemShape(2, 3),
                        onClick = { viewModel.onThemeChanged(OlpakaTheme.LIGHT) },
                    )
                }
            }
            SettingItem("Color") {
                Row {
                    OlpakaColor.entries.forEach {
                        color(
                            color = it,
                            selected = state.selectedColor == it,
                            onClick = viewModel::onColorChanged
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1.0f))
            SettingLink(
                title = "Troubleshooting",
                subtitle = "Launch the \"Getting Started\" wizard",
                onClick = {
                    navHostController.navigate(Routes.ONBOARDING)
                }
            )
            SettingLink(
                title = "Olpaka v0.5.0",
                subtitle = "Learn more about Olpaka",
                onClick = {
                    navHostController.navigate(Routes.ABOUT)
                }
            )
        }
    }
}

@Composable
private fun color(color: OlpakaColor, selected: Boolean, onClick: (OlpakaColor) -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val fillColor = when (color) {
        OlpakaColor.OLPAKA -> olpakaColorOlpaka
        OlpakaColor.RED -> olpakaColorRed
        OlpakaColor.PURPLE -> olpakaColorPurple
        OlpakaColor.BLUE -> olpakaColorBlue
        OlpakaColor.ORANGE -> olpakaColorOrange
        OlpakaColor.GREEN -> olpakaColorGreen
        OlpakaColor.GREY -> olpakaColorGrey
    }
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(CircleShape)
            .clickable { onClick(color) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.size(32.dp),
            onDraw = {
                drawCircle(
                    color = fillColor,
                )
                drawCircle(
                    color = outlineColor,
                    style = Stroke(width = 2.0f),
                )
            },
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun SettingSection(title: String) {
    Text(
        title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        style = MaterialTheme.typography.headlineSmall,
    )
}

@Composable
private fun SettingItem(name: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            modifier = Modifier.weight(1.0f),
        )
        content()
    }
}

@Composable
private fun SettingLink(title: String, subtitle: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable { onClick() }) {
        HorizontalDivider(Modifier.height(1.dp))
        Column(
            Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(subtitle)
        }
    }
}