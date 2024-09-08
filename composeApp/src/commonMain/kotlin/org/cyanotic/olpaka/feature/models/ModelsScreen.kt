package org.cyanotic.olpaka.feature.models

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.cyanotic.olpaka.ui.OlpakaAppBar
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
                title = "Models",
                actions = {
                    IconButton(
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
            FloatingActionButton(
                onClick = { viewModel.onAddModelClicked() },
            ) {
                Icon(Icons.Outlined.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(
                count = state.models.count(),
                key = { index -> state.models[index].key }
            ) { index ->
                when (val model = state.models[index]) {
                    is ModelUI.Available -> ModelAvailable(
                        model = model,
                        onRemoveClicked = { viewModel.onRemoveModel(model) },
                    )

                    is ModelUI.Downloading -> ModelDownloading(
                        model = model,
                        onCancelClicked = { viewModel.onCancelDownload(model) },
                    )

                    is ModelUI.Error -> ModelError(model)
                }
                HorizontalDivider()
            }
        }
    }

}

@Composable
private fun AddModelDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        title = {
            Text(text = "Download ollama model")
        },
        text = {
            Column {
                DialogHyperLink()
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
                    label = { Text("e.g. \"llama3:latest\" or just \"llama3\"") }
                )
            }

        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank(),
                onClick = { onConfirm(text) }
            ) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DialogHyperLink() {
    val annotatedText = buildAnnotatedString {
        append("Pick a model from ")
        withLink(
            LinkAnnotation.Url(
                url = "https://ollama.com/library",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append("the ollama library")
        }
        append(" and add its name here.")
    }
    Text(annotatedText)
}

@Composable
private fun ModelAvailable(model: ModelUI.Available, onRemoveClicked: () -> Unit) {
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
        Button(onClick = onRemoveClicked) {
            Text("Remove")
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
            Text("Cancel")
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