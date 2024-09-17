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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import olpaka.composeapp.generated.resources.*
import org.cyanotic.olpaka.ui.EmptyScreen
import org.cyanotic.olpaka.ui.OlpakaAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ModelsScreen() {
    val viewModel = koinViewModel<ModelsViewModel>().also { it.init() }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.event.collect { }
    }

    when(val currentState = state){
        is ModelsState.Content -> Content(
            state =  currentState,
            onCloseAddModelDialog = viewModel::onCloseAddModelDialog,
            onAddModel = viewModel::onAddModel,
            onAddModelTextChanged = viewModel::onAddModelTextChanged,
            onRemoveModelDialogResult = viewModel::onRemoveModelDialogResult,
            onRefreshClicked = viewModel::onRefreshClicked,
            onAddModelClicked = viewModel::onAddModelClicked,
            onRemoveModelClicked = viewModel::onRemoveModelClicked,
            onCancelDownload = viewModel::onCancelDownload
        )
        ModelsState.Error -> Error(
            onRefreshClicked = viewModel::onRefreshClicked
        )
        ModelsState.Loading -> Loading()
    }
}

@Composable
private fun Content(
    state: ModelsState.Content,
    onCloseAddModelDialog: () -> Unit,
    onAddModel: (String) -> Unit,
    onAddModelTextChanged: (String) -> Unit,
    onRemoveModelDialogResult: (Boolean) -> Unit,
    onRefreshClicked: () -> Unit,
    onAddModelClicked: () -> Unit,
    onRemoveModelClicked: (model: ModelUI.Available) -> Unit,
    onCancelDownload: () -> Unit
) {
    var addModelTextState by remember { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }


    val addModelDialogState = state.addModelDialogState
    val removeModelDialogState = state.removeModelDialogState
    if(addModelDialogState == null){
        addModelTextState = TextFieldValue()
    }
    if (addModelDialogState != null) {
        AddModelDialog(
            onDismiss = onCloseAddModelDialog,
            onConfirm = { onAddModel(addModelTextState.text) },
            textState = addModelTextState,
            onTextValueChange = {
                val newText = it.text.filter { char -> !char.isWhitespace() }
                addModelTextState = it.copy(text = newText)
                onAddModelTextChanged(addModelTextState.text)
            },
            confirmEnabled = addModelDialogState.isAddEnabled,
            error = addModelDialogState.error,
            focusRequester = focusRequester,
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    } else if (removeModelDialogState != null) {
        RemoveModelDialog(
            modelName = removeModelDialogState.model,
            onDismiss = { onRemoveModelDialogResult(false) },
            onConfirm = { onRemoveModelDialogResult(true) }
        )
    }
    Scaffold(
        topBar = {
            OlpakaAppBar(
                title = stringResource(Res.string.models_title),
                actions = {
                    IconButton(
                        enabled = !state.isLoading,
                        onClick = onRefreshClicked
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
                    onClick = onAddModelClicked,
                ) {
                    Icon(Icons.Outlined.Add, null)
                }
            }
        }
    ) { padding ->
        if (state.models.isEmpty()) {
            EmptyScreen(
                modifier = Modifier.fillMaxSize(),
                title = stringResource(Res.string.models_error_no_models_title),
                subtitle = stringResource(Res.string.models_error_no_models_message)
            )
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(
                    count = state.models.count(),
                    key = { index -> state.models[index].key }
                ) { index ->
                    when (val model = state.models[index]) {
                        is ModelUI.Available -> ModelAvailable(
                            model = model,
                            onRemoveClicked = { onRemoveModelClicked(model) },
                            removeEnabled = !state.isLoading
                        )

                        is ModelUI.Downloading -> ModelDownloading(
                            model = model,
                            onCancelClicked = onCancelDownload,
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
private fun Loading(){
    Scaffold(
        topBar = {
            OlpakaAppBar(
                title = stringResource(Res.string.models_title),
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Fetching Models")
        }
    }
}

@Composable
private fun Error(
    onRefreshClicked: () -> Unit
){
    Scaffold(
        topBar = {
            OlpakaAppBar(
                title = stringResource(Res.string.models_title),
                actions = {
                    IconButton(
                        onClick = onRefreshClicked
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null,
                        )
                    }
                }
            )
        }
    ) {
        EmptyScreen(
            modifier = Modifier.fillMaxSize(),
            title = stringResource(Res.string.error_missing_ollama_title),
            subtitle = stringResource(Res.string.error_missing_ollama_message)
        )
    }
}

@Composable
private fun AddModelDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    textState: TextFieldValue,
    onTextValueChange: (TextFieldValue) -> Unit,
    confirmEnabled: Boolean,
    focusRequester: FocusRequester,
    error: String?,
) {
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
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (confirmEnabled) {
                                onConfirm()
                            }
                        }
                    ),
                    supportingText = {
                        error?.let { Text(error, color = MaterialTheme.colorScheme.error) }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    value = textState,
                    onValueChange = onTextValueChange,
                    label = { Text(stringResource(Res.string.models_dialog_download_model_text_hint)) }
                )
            }

        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = confirmEnabled,
                onClick = onConfirm
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
private fun RemoveModelDialog(
    modelName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        title = {
            Text(text = stringResource(Res.string.models_dialog_remove_model_title))
        },
        text = {
            Text(stringResource(Res.string.models_dialog_remove_model_description, modelName))
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(Res.string.models_dialog_remove_model_action_positive))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.models_dialog_remove_model_action_negative))
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
    ListItem(
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.DownloadForOffline,
                contentDescription = null
            )
        },
        headlineContent = {
            Text(model.title)
        },
        supportingContent = {
            Text(model.subtitle)
        },
        trailingContent = {
            FilledTonalButton(
                enabled = removeEnabled,
                onClick = onRemoveClicked
            ) {
                Text(stringResource(Res.string.models_action_remove_model))
            }
        }
    )
}

@Composable
private fun ModelDownloading(model: ModelUI.Downloading, onCancelClicked: () -> Unit) {
    ListItem(
        leadingContent = {
            if (model.progress != null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    progress = { model.progress }
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        headlineContent = {
            Text(model.title)
        },
        supportingContent = {
            Text(model.subtitle)
        },
        trailingContent = {
            FilledTonalButton(onClick = onCancelClicked) {
                Text(stringResource(Res.string.models_action_cancel_download))
            }
        }
    )
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