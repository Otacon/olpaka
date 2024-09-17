package org.cyanotic.olpaka.feature.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
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
import com.cyanotic.olpaka.BuildKonfig
import olpaka.composeapp.generated.resources.*
import org.cyanotic.olpaka.core.Routes
import org.cyanotic.olpaka.ui.OlpakaAppBar
import org.cyanotic.olpaka.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(navHostController: NavHostController) {
    val viewModel = koinViewModel<SettingsViewModel>().also { it.init() }
    val state by viewModel.state.collectAsState()

    SettingsScreenContent(
        state = state,
        onColorChanged = viewModel::onColorChanged,
        onThemeChanged = viewModel::onThemeChanged,
        onConnectionHostChanged = viewModel::onConnectionUrlChanged,
        onResetHostClicked = viewModel::revertDefaultConnectionUrl,
        onClearPreferencesClicked = viewModel::onClearPreferencesClicked,
        onOnboardingClicked = { navHostController.navigate(Routes.ONBOARDING) },
        onAboutClicked = { navHostController.navigate(Routes.ABOUT) }
    )
}

@Composable
@Preview
private fun SettingsScreenContent(
    state: SettingsState = SettingsState(),
    onColorChanged: (OlpakaColor) -> Unit = {},
    onThemeChanged: (OlpakaTheme) -> Unit = {},
    onConnectionHostChanged: (String) -> Unit = {},
    onResetHostClicked: () -> Unit = {},
    onClearPreferencesClicked: () -> Unit = {},
    onOnboardingClicked: () -> Unit = {},
    onAboutClicked: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = { OlpakaAppBar(stringResource(Res.string.settings_title)) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
            ) {
                SettingSection(stringResource(Res.string.settings_theme_section))
                SettingItem(stringResource(Res.string.settings_theme_mode)) {
                    ThemeSelector(
                        selectedTheme = state.selectedTheme,
                        onThemeChanged = onThemeChanged
                    )
                }
                SettingItem(stringResource(Res.string.settings_theme_color)) {
                    ColorSelector(
                        selectedColor = state.selectedColor,
                        onColorChanged = onColorChanged
                    )
                }
                SettingSection(stringResource(Res.string.settings_connection_section))
                SettingItem(stringResource(Res.string.settings_connection_url)) {
                    TextField(
                        value = state.connectionHost,
                        onValueChange = onConnectionHostChanged,
                        singleLine = true,
                        supportingText = {
                            state.hostError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = onResetHostClicked) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Undo,
                                    contentDescription = "Reset to default",
                                )
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.weight(1.0f))
            if(BuildKonfig.allowClearPreferences){
                SettingLink(
                    title = stringResource(Res.string.settings_clear_preferences_title),
                    subtitle = stringResource(Res.string.settings_clear_preferences_subtitle),
                    onClick = onClearPreferencesClicked
                )
            }
            SettingLink(
                title = stringResource(Res.string.settings_onboarding_title),
                subtitle = stringResource(Res.string.settings_onboarding_subtitle),
                onClick = onOnboardingClicked
            )
            SettingLink(
                title = "${stringResource(Res.string.app_name)} (${BuildKonfig.appVersion}+${BuildKonfig.appVariant})",
                subtitle = stringResource(Res.string.settings_about_subtitle),
                onClick = onAboutClicked
            )
        }
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: OlpakaTheme,
    onThemeChanged: (OlpakaTheme) -> Unit,
) {
    SingleChoiceSegmentedButtonRow {
        OlpakaTheme.entries.forEachIndexed { index, theme ->
            val isSelected = selectedTheme == theme
            val icon = if (isSelected) {
                Icons.Filled.Done
            } else {
                when (theme) {
                    OlpakaTheme.AUTO -> Icons.Outlined.BrightnessAuto
                    OlpakaTheme.LIGHT -> Icons.Outlined.LightMode
                    OlpakaTheme.DARK -> Icons.Outlined.DarkMode
                }
            }
            val text = when (theme) {
                OlpakaTheme.AUTO -> stringResource(Res.string.settings_theme_mode_system)
                OlpakaTheme.LIGHT -> stringResource(Res.string.settings_theme_mode_light)
                OlpakaTheme.DARK -> stringResource(Res.string.settings_theme_mode_dark)
            }
            SegmentedButton(
                icon = {
                    Icon(
                        icon,
                        contentDescription = null
                    )
                },
                label = { Text(text) },
                selected = isSelected,
                shape = SegmentedButtonDefaults.itemShape(index, OlpakaTheme.entries.size),
                onClick = { onThemeChanged(theme) },
            )
        }
    }
}

@Composable
private fun ColorSelector(
    selectedColor: OlpakaColor,
    onColorChanged: (OlpakaColor) -> Unit,
) {
    Row {
        OlpakaColor.entries.forEach { entry ->
            ColorItem(
                color = entry,
                selected = selectedColor == entry,
                onClick = { onColorChanged(it) }
            )
        }
    }
}

@Composable
private fun ColorItem(
    color: OlpakaColor,
    selected: Boolean,
    onClick: (OlpakaColor) -> Unit
) {
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
private fun SettingSection(
    title: String
) {
    Text(
        title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        style = MaterialTheme.typography.headlineSmall,
    )
}

@Composable
private fun SettingItem(
    name: String,
    content: @Composable () -> Unit
) {
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
private fun SettingLink(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
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