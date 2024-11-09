package org.cyanotic.olpaka.feature.chat

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import io.github.aakira.napier.Napier
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.app_name
import olpaka.composeapp.generated.resources.chat_assistant_name
import olpaka.composeapp.generated.resources.chat_message_error
import olpaka.composeapp.generated.resources.chat_model_dropdown_hint
import olpaka.composeapp.generated.resources.chat_text_input_hint
import olpaka.composeapp.generated.resources.chat_user_name
import olpaka.composeapp.generated.resources.error_missing_ollama_positive
import olpaka.composeapp.generated.resources.loading
import org.cyanotic.olpaka.ui.EmptyScreen
import org.cyanotic.olpaka.ui.OlpakaAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun ChatScreen() {
    val viewModel = koinViewModel<ChatViewModel>()
    val state by viewModel.state.collectAsState()
    var textState by remember { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }
    val chatListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.onCreate()
        viewModel.event.collect { event ->
            when (event) {
                ChatEvent.ClearTextInput -> textState = TextFieldValue("")
                ChatEvent.FocusOnTextInput -> {
                    try {
                        focusRequester.requestFocus()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    val currentState = state
    Napier.d(tag = "Chat Screen", message = "Showing $currentState")
    if (currentState is ChatState.Content) {
        LaunchedEffect(currentState) {
            chatListState.animateScrollToItem(chatListState.layoutInfo.totalItemsCount)
        }
    }
    ChatView(
        state = currentState,
        listState = chatListState,
        onRefresh = viewModel::onRefresh,
        onAddNewLine = { textState = textState.addNewLine() },
        onTextChanged = { textState = it },
        onSubmit = viewModel::onSubmit,
        textState = textState,
        onModelSelected = viewModel::onModelChanged,
        userInputFocusRequester = focusRequester,
    )
}

@Composable
private fun ChatView(
    state: ChatState,
    listState: LazyListState,
    textState: TextFieldValue,
    onRefresh: () -> Unit,
    onAddNewLine: () -> Unit,
    onTextChanged: (TextFieldValue) -> Unit,
    onSubmit: (String) -> Unit,
    onModelSelected: (ChatModelUI) -> Unit,
    userInputFocusRequester: FocusRequester,
) {
    Scaffold(
        topBar = { OlpakaAppBar(stringResource(Res.string.app_name)) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
        ) {
            MessageList(
                state = state,
                listState = listState,
                onRefresh = onRefresh,
            )

            if (state is ChatState.Content) {
                InputBar(
                    state,
                    textState = textState,
                    onAddNewLine = onAddNewLine,
                    onTextChanged = onTextChanged,
                    onSubmit = onSubmit,
                    onModelSelected = onModelSelected,
                    focusRequester = userInputFocusRequester,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.MessageList(
    state: ChatState,
    listState: LazyListState,
    onRefresh: () -> Unit
) {
    when (state) {
        is ChatState.Content -> Content(
            modifier = Modifier.fillMaxWidth().weight(1.0f),
            chatListState = listState,
            messages = state.messages,
        )

        is ChatState.Error -> EmptyScreen(
            modifier = Modifier.fillMaxWidth().weight(1.0f),
            title = state.title,
            subtitle = state.message,
            cta = {
                if (state.showTryAgain) {
                    Button(
                        modifier = Modifier.align(Alignment.End),
                        onClick = onRefresh,
                    ) {
                        Text(stringResource(Res.string.error_missing_ollama_positive))
                    }
                }
            }
        )

        ChatState.Loading -> Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(Res.string.loading))
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
                is ChatMessageUI.Assistant -> AssistantMessage(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    message = item
                )

                is ChatMessageUI.User -> OwnMessage(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    message = item,
                )
            }
        }
    }
}

@Composable
private fun InputBar(
    state: ChatState.Content,
    textState: TextFieldValue,
    onAddNewLine: () -> Unit,
    onTextChanged: (TextFieldValue) -> Unit,
    onSubmit: (String) -> Unit,
    onModelSelected: (ChatModelUI) -> Unit,
    focusRequester: FocusRequester,
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1.0f)
                .onPreviewKeyEvent { e ->
                    if (e.type == KeyEventType.KeyDown && (e.key == Key.Enter || e.key == Key.NumPadEnter)) {
                        if (e.isShiftPressed) {
                            onAddNewLine()
                        } else {
                            onSubmit(textState.text)
                        }
                        true
                    } else {
                        false
                    }
                },
            value = textState,
            onValueChange = onTextChanged,
            enabled = state.controlsEnabled,
            minLines = 1,
            maxLines = 5,
            placeholder = { Text(stringResource(Res.string.chat_text_input_hint)) },
            trailingIcon = {
                IconButton(
                    enabled = state.controlsEnabled && textState.text.isNotBlank(),
                    onClick = { onSubmit(textState.text) }
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
            options = state.models,
            selectedModel = state.selectedModel,
            enabled = state.controlsEnabled,
            onOptionSelected = onModelSelected,
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
                if (enabled) {
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
private fun OwnMessage(
    modifier: Modifier = Modifier,
    message: ChatMessageUI.User,
) {
    Card(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                stringResource(Res.string.chat_user_name),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(8.dp))
            SelectionContainer { Markdown(message.text) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssistantMessage(
    modifier: Modifier = Modifier,
    message: ChatMessageUI.Assistant,
) {
    OutlinedCard(
        modifier = modifier
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.chat_assistant_name),
                    style = MaterialTheme.typography.headlineSmall
                )
                if (message.isGenerating) {
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else if (message.isError) {
                    Spacer(Modifier.width(8.dp))
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip {
                                Text(stringResource(Res.string.chat_message_error))
                            }
                        },
                        state = rememberTooltipState()
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            SelectionContainer { Markdown(message.text) }
        }
    }
}

private fun TextFieldValue.addNewLine(): TextFieldValue {
    val newText = this.text + "\n"
    val newSelection = TextRange(newText.length)
    return this.copy(newText, selection = newSelection)
}

@Preview
@Composable
private fun ChatViewPreview() {
    val state = ChatState.Content(
        messages = listOf(
            ChatMessageUI.User(
                text = "Why is the sky blue?"
            ),
            ChatMessageUI.Assistant(
                text = "This is because ",
                isGenerating = true,
                isError = false
            )
        ),
        models = listOf(
            ChatModelUI("qwen:0.5b", "qwen:0.5b")
        ),
        selectedModel = ChatModelUI("qwen:0.5b", "qwen:0.5b"),
        controlsEnabled = true
    )
    ChatView(
        state = state,
        listState = LazyListState(),
        textState = TextFieldValue("Something"),
        onRefresh = {},
        onAddNewLine = {},
        onTextChanged = {},
        onSubmit = {},
        onModelSelected = {},
        userInputFocusRequester = FocusRequester()
    )
}
