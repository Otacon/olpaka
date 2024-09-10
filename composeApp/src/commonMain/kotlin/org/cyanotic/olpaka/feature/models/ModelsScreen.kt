package org.cyanotic.olpaka.feature.models

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import olpaka.composeapp.generated.resources.*
import org.cyanotic.olpaka.ui.EmptyScreen
import org.cyanotic.olpaka.ui.OlpakaAppBar
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun ModelsScreen() {
    val viewModel = koinViewModel<ModelsViewModel>().also { it.init() }
    val state by viewModel.state.collectAsState()
    var openAddModelDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                ModelsEvent.ShowAddModelDialog -> openAddModelDialog = true
            }
        }
    }
    if (openAddModelDialog) {
        AddModelDialog(
            onDismiss = { openAddModelDialog = false },
            onConfirm = { model ->
                openAddModelDialog = false
                viewModel.onAddModel(model)
            }
        )
    }
    Scaffold(
        topBar = {
            OlpakaAppBar(
                title = stringResource(Res.string.models_title),
                actions = {
                    IconButton(
                        enabled = !state.isLoading,
                        onClick = viewModel::onRefreshClicked
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null,
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!state.isLoading) {
                FloatingActionButton(
                    onClick = { viewModel.onAddModelClicked() },
                ) {
                    Icon(Icons.Outlined.Add, null)
                }
            }
        }
    ) { padding ->
        if(state.models.isEmpty()){
            EmptyScreen(
                modifier = Modifier.padding(padding),
                title = stringResource(Res.string.models_error_no_models_title),
                subtitle = stringResource(Res.string.models_error_no_models_message)
            )
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(
                    count = state.models.count(),
                    key = { index -> state.models[index].key }
                ) { index ->
                    when (val model = state.models[index]) {
                        is ModelUI.Available -> ModelAvailable(
                            model = model,
                            onRemoveClicked = { viewModel.onRemoveModel(model) },
                            removeEnabled = !state.isLoading
                        )

                        is ModelUI.Downloading -> ModelDownloading(
                            model = model,
                            onCancelClicked = { viewModel.onCancelDownload() },
                        )

                        is ModelUI.Error -> ModelError(model)
                    }
                    HorizontalDivider()
                }
            }
        }
    }

}

@Composable
private fun AddModelDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        title = {
            Text(text = stringResource(Res.string.models_dialog_download_model_title))
        },
        text = {
            Column {
                Markdown(
                    stringResource(Res.string.models_dialog_download_model_description),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardActions = KeyboardActions(
                        onDone = { if (text.isNotBlank()) onConfirm(text) }
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(Res.string.models_dialog_download_model_text_hint)) }
                )
            }

        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank(),
                onClick = { onConfirm(text) }
            ) {
                Text(stringResource(Res.string.models_dialog_download_model_action_positive))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.models_dialog_download_model_action_negative))
            }
        }
    )
}

@Composable
private fun ModelAvailable(
    model: ModelUI.Available,
    onRemoveClicked: () -> Unit,
    removeEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            modifier = Modifier.align(Alignment.CenterVertically),
            imageVector = Icons.Outlined.DownloadForOffline,
            contentDescription = null
        )
        Column(
            Modifier.weight(1.0f)
                .padding(horizontal = 16.dp)
        ) {
            Text(model.title)
            Spacer(modifier = Modifier.height(4.dp))
            Text(model.subtitle, style = MaterialTheme.typography.labelSmall)
        }
        Button(
            enabled = removeEnabled,
            onClick = onRemoveClicked
        ) {
            Text(stringResource(Res.string.models_action_remove_model))
        }
    }
}

@Composable
private fun ModelDownloading(model: ModelUI.Downloading, onCancelClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (model.progress != null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically),
                progress = { model.progress }
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
        }
        Column(
            Modifier.weight(1.0f)
                .padding(horizontal = 16.dp)
        ) {
            Text(model.title)
            Text(model.subtitle)
        }
        Button(onClick = onCancelClicked) {
            Text(stringResource(Res.string.models_action_cancel_download))
        }
    }
}


@Composable
private fun ModelError(model: ModelUI.Error) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            modifier = Modifier.align(Alignment.CenterVertically),
            imageVector = Icons.Outlined.Storage,
            contentDescription = null
        )
        Column(
            Modifier.weight(1.0f)
                .padding(horizontal = 16.dp)
        ) {
            Text(model.title)
            Text(model.subtitle)
        }
    }
}