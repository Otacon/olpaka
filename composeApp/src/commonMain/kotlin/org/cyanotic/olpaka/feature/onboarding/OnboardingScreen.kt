package org.cyanotic.olpaka.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import olpaka.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun OnboardingScreen(navController: NavHostController) {
    val viewModel = koinViewModel<OnboardingViewModel>().also { it.init() }
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(Unit) {
        viewModel.onCreate()
        viewModel.event.collect { event ->
            when (event) {
                OnboardingEvent.Close -> navController.popBackStack()
                is OnboardingEvent.OpenBrowser -> uriHandler.openUri(event.url)
            }
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = stringResource(Res.string.onboarding_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Custom Content (replace with your custom layout)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                when (state.currentStep) {
                    0 -> StepOne()
                    1 -> StepTwo(
                        onDownloadOllamaClicked = viewModel::onDownloadOllamaClicked
                    )

                    2 -> StepThree(
                        connectionCheckState = state.connectionState,
                        onSetupCorsClicked = viewModel::onSetupCorsClicked,
                        onCheckConnectionClicked = viewModel::onCheckConnectionClicked
                    )

                    else -> throw IllegalStateException("Unknown onboarding step ${state.currentStep}")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (state.isPreviousVisible) {
                    TextButton(
                        onClick = viewModel::onPreviousPressed,
                    ) {
                        Text(stringResource(Res.string.onboarding_cta_previous))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(
                    onClick = viewModel::onNextPressed
                ) {
                    Text(state.nextText)
                }
            }
        }
    }
}

@Composable
private fun StepOne() {
    Text(stringResource(Res.string.onboarding_step_1))
}

@Composable
private fun StepTwo(
    onDownloadOllamaClicked: () -> Unit
) {
    Text(stringResource(Res.string.onboarding_step_2_a))
    Button(
        onClick = onDownloadOllamaClicked,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(stringResource(Res.string.onboarding_download_ollama))
    }
    Text(stringResource(Res.string.onboarding_step_2_b))
}

@Composable
private fun StepThree(
    connectionCheckState: ConnectionCheckState,
    onSetupCorsClicked: () -> Unit,
    onCheckConnectionClicked: () -> Unit,
) {
    Text(stringResource(Res.string.onboarding_step_3_a))
    Button(
        onClick = onSetupCorsClicked,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(stringResource(Res.string.onboarding_setup_cors))
    }
    Text(stringResource(Res.string.onboarding_step_3_b))
    Button(
        onClick = onCheckConnectionClicked,
        modifier = Modifier.padding(vertical = 8.dp),
    ) {
        val (icon, text) = when (connectionCheckState) {
            ConnectionCheckState.UNKNOWN -> Icons.Default.QuestionMark to stringResource(Res.string.onboarding_check_connection_unknown)
            ConnectionCheckState.SUCCESS -> Icons.Default.CheckCircleOutline to stringResource(Res.string.onboarding_check_connection_success)
            ConnectionCheckState.FAILURE -> Icons.Default.WarningAmber to stringResource(Res.string.onboarding_check_connection_failure)
        }

        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text)
    }
    Text(stringResource(Res.string.onboarding_step_3_c))
}