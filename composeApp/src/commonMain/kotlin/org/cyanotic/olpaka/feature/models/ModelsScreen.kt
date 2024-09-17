package org.cyanotic.olpaka.feature.models

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import olpaka.composeapp.generated.resources.*
import org.cyanotic.olpaka.core.Routes
import org.cyanotic.olpaka.ui.EmptyScreen
import org.cyanotic.olpaka.ui.OlpakaAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ModelsScreen(navController: NavController) {
    val viewModel = koinViewModel<ModelsViewModel>().also { it.init() }
    val state by viewModel.state.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val removeModel = backStackEntry?.savedStateHandle?.get<String>("removeModel")
    val addModel = backStackEntry?.savedStateHandle?.get<String>("addModel")

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ModelsEvent.OpenRemoveModelDialog ->
                    navController.navigate(Routes.MODELS_REMOVE_MODEL_DIALOG + event.key)


                ModelsEvent.OpenAddModelDialog -> navController.navigate(Routes.MODELS_ADD_MODEL_DIALOG)
            }
        }
    }

    LaunchedEffect(removeModel) {
        removeModel?.let { viewModel.onConfirmRemoveModel(it) }
    }
    LaunchedEffect(addModel) {
        addModel?.let { viewModel.onAddModel(it) }
    }

    when (val currentState = state) {
        is ModelsState.Content -> Content(
            state = currentState,
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
    onRefreshClicked: () -> Unit,
    onAddModelClicked: () -> Unit,
    onRemoveModelClicked: (model: ModelUI.Available) -> Unit,
    onCancelDownload: () -> Unit
) {
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
private fun Loading() {
    Scaffold(
        topBar = {
            OlpakaAppBar(
                title = stringResource(Res.string.models_title),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Fetching Models")
        }
    }
}

@Composable
private fun Error(
    onRefreshClicked: () -> Unit
) {
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