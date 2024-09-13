package org.cyanotic.olpaka.feature.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import olpaka.composeapp.generated.resources.*
import org.cyanotic.olpaka.ui.EmptyScreen
import org.cyanotic.olpaka.ui.OlpakaAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatScreen() {
    val viewModel = koinViewModel<ChatViewModel>().also { it.init() }
    val state by viewModel.state.collectAsState()
    val chatListState = rememberLazyListState()
    LaunchedEffect(Unit) {
        viewModel.onCreate()
        viewModel.event.collect { _ ->

        }
    }
    LaunchedEffect(state.messages) {
        chatListState.animateScrollToItem(chatListState.layoutInfo.totalItemsCount)
    }
    Scaffold(
        topBar = { OlpakaAppBar(stringResource(Res.string.app_name)) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
        ) {
            if (state.messages.isEmpty()) {
                EmptyScreen(
                    modifier = Modifier.fillMaxWidth().weight(1.0f),
                    title = stringResource(Res.string.chat_empty_screen_title),
                    subtitle = stringResource(Res.string.chat_empty_screen_message)
                )
            } else {
                Content(
                    modifier = Modifier.fillMaxWidth().weight(1.0f),
                    chatListState = chatListState,
                    messages = state.messages
                )
            }
            MessageInputBar(
                models = state.models,
                selectedModel = state.selectedModel,
                enabled = state.isLoading.not(),
                onSubmit = viewModel::onSubmit,
                onModelChanged = viewModel::onModelChanged,
            )
        }
    }
}

@Composable
private fun Content(
    modifier: Modifier,
    chatListState: LazyListState,
    messages: List<ChatMessageUI>
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        state = chatListState
    ) {
        items(
            messages.size,
            key = { index -> "$index" }
        ) { index ->
            when (val item = messages[index]) {
                is ChatMessageUI.AssistantMessage -> AssistantMessage(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    message = item
                )

                is ChatMessageUI.OwnMessage -> OwnMessage(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    message = item,
                )
            }
        }
    }
}

@Composable
private fun MessageInputBar(
    models: List<ChatModelUI>,
    selectedModel: ChatModelUI? = null,
    enabled: Boolean = true,
    onSubmit: (String) -> Unit,
    onModelChanged: (ChatModelUI) -> Unit,
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val sendIfNotBlank = {
        val text = textState.text
        if(text.isNotBlank()) {
            onSubmit(text)
            textState = TextFieldValue("")
        }
    }
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = textState,
            onValueChange = { newText -> textState = newText },
            enabled = enabled,
            modifier = Modifier
                .weight(1.0f)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) {
                        return@onPreviewKeyEvent false
                    }

                    if (event.key != Key.Enter) {
                        return@onPreviewKeyEvent false
                    }

                    if (event.isShiftPressed) {
                        textState = textState.copy(
                            text = textState.text + "\n",
                            selection = TextRange(
                                start = textState.selection.start + 1,
                                end = textState.selection.end + 1
                            )
                        )
                    } else {
                        sendIfNotBlank()
                    }
                    true
                },
            minLines = 1,
            maxLines = 5,
            placeholder = { Text(stringResource(Res.string.chat_text_input_hint)) },
            trailingIcon = {
                IconButton(
                    enabled = enabled && textState.text.isNotBlank(),
                    onClick = sendIfNotBlank
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = null
                    )
                }
            },
        )
        Spacer(modifier = Modifier.width(16.dp))
        DropDown(
            modifier = Modifier.width(200.dp),
            options = models,
            selectedModel = selectedModel,
            enabled = enabled,
            onOptionSelected = onModelChanged,
        )
    }
}

@Composable
private fun DropDown(
    modifier: Modifier = Modifier,
    options: List<ChatModelUI> = emptyList(),
    selectedModel: ChatModelUI? = null,
    enabled: Boolean = true,
    onOptionSelected: (ChatModelUI) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var model by remember { mutableStateOf(selectedModel) }
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .clickable {
                if(enabled) {
                    expanded = !expanded
                }
           },
    ) {
        TextField(
            value = selectedModel?.name ?: "",
            enabled = false,
            onValueChange = { },
            placeholder = { Text(stringResource(Res.string.chat_model_dropdown_hint)) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption.name) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        model = selectedModel
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun OwnMessage(modifier: Modifier = Modifier, message: ChatMessageUI.OwnMessage) {
    Card(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(stringResource(Res.string.chat_user_name), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            SelectionContainer { Markdown(message.text) }
        }
    }
}

@Composable
private fun AssistantMessage(modifier: Modifier = Modifier, message: ChatMessageUI.AssistantMessage) {
    OutlinedCard(
        modifier = modifier
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(stringResource(Res.string.chat_assistant_name), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            SelectionContainer { Markdown(message.text) }
        }
    }
}
