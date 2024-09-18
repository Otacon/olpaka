package org.cyanotic.olpaka.feature.models

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mikepenz.markdown.m3.Markdown
import olpaka.composeapp.generated.resources.*
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.models_dialog_download_model_title
import org.cyanotic.olpaka.core.Results.RESULT_ADD_MODEL_KEY
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ModelsAddModelScreen(navController: NavController) {
    val viewModel = koinViewModel<ModelsAddModelViewModel>().also { it.init() }
    val state by viewModel.state.collectAsState()
    val focusRequester = FocusRequester()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        viewModel.onCreate()
        viewModel.event.collect { event ->
            when (event) {
                AddModelEvent.Cancel -> navController.popBackStack()
                is AddModelEvent.Confirm -> {
                    val previousBackStackEntry = navController.previousBackStackEntry
                    previousBackStackEntry?.savedStateHandle?.set(RESULT_ADD_MODEL_KEY, event.model)
                    navController.popBackStack()
                }
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
                text = stringResource(Res.string.models_dialog_download_model_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column {
                Markdown(
                    stringResource(Res.string.models_dialog_download_model_description),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    singleLine = true,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (state.okButtonEnabled) {
                                viewModel.onOkClicked()
                            }
                        }
                    ),
                    supportingText = {
                        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    value = state.modelToAdd,
                    onValueChange = { text -> viewModel.onModelNameChanged(text)},
                    label = { Text(stringResource(Res.string.models_dialog_download_model_text_hint)) }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = viewModel::onCancelClicked,
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    enabled = state.okButtonEnabled,
                    onClick = viewModel::onOkClicked
                ) {
                    Text("Download")
                }
            }
        }
    }
}