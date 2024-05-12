import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:get_it/get_it.dart';
import 'package:markdown_widget/markdown_widget.dart';
import 'package:olpaka/feature/chat/events.dart';
import 'package:olpaka/feature/chat/state.dart';
import 'package:olpaka/feature/chat/view_model.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class ChatScreen extends StatelessWidget {
  const ChatScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<ChatViewModel>.reactive(
      viewModelBuilder: () => GetIt.I.get(),
      onViewModelReady: (viewModel) {
        viewModel.events.listen((event) {
          switch (event) {
            case GenericError():
              _showErrorDialog(
                  context: context,
                  title: event.title,
                  message: event.message,
                  positive: S.current.error_generic_positive,
                  positiveAction: () => {});
            case OllamaNotFound():
              _showErrorDialog(
                  context: context,
                  title: event.title,
                  message: event.message,
                  positive: event.positive,
                  positiveAction: () => {viewModel.onRefresh()});
            case ModelNotFound():
              _showErrorDialog(
                  context: context,
                  title: event.title,
                  message: event.message,
                  positive: event.positive,
                  positiveAction: () => {});
          }
        });
        viewModel.onCreate();
      },
      builder: (context, viewModel, child) {
        final state = viewModel.state;
        return Scaffold(
          appBar: AppBar(
            elevation: 4,
            shadowColor: Theme.of(context).shadowColor,
            centerTitle: true,
            title: Text(
              S.current.app_name,
              style: Theme.of(context).textTheme.headlineMedium,
            ),
          ),
          body: _Content(
            messages: state.messages,
            models: state.models,
            selectedModel: state.selectedModel,
            isEnabled: !state.isLoading,
            onModelSelected: viewModel.onModelChanged,
            onSendMessage: viewModel.onSendMessage,
          ),
        );
      },
    );
  }

  _showErrorDialog({
    required BuildContext context,
    required String title,
    required String message,
    required String positive,
    required Function() positiveAction,
  }) {
    showDialog(
      barrierDismissible: false,
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text(title),
          content: Text(message),
          actions: [
            TextButton(
                onPressed: () =>
                    {Navigator.of(context).pop(false), positiveAction()},
                child: Text(positive))
          ],
        );
      },
    );
  }
}

class _Content extends StatelessWidget {
  _Content({
    required this.messages,
    required this.models,
    required this.onSendMessage,
    required this.onModelSelected,
    this.isEnabled = true,
    this.selectedModel,
  });

  final List<ChatMessage> messages;
  final List<ChatModel> models;
  final ChatModel? selectedModel;
  final bool isEnabled;
  final Function(String) onSendMessage;
  final Function(ChatModel?) onModelSelected;
  final ScrollController _scrollController = ScrollController();

  @override
  Widget build(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;
    final messageMinWidth = 0.1 * screenWidth;
    final messageMaxWidth = 0.9 * screenWidth;
    SchedulerBinding.instance.addPostFrameCallback((_) {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent,
        duration: const Duration(milliseconds: 3),
        curve: Curves.linear,
      );
    });
    return Column(
      children: [
        Expanded(
          child: ListView.builder(
              controller: _scrollController,
              itemCount: messages.length,
              itemBuilder: (context, index) {
                final message = messages[index];
                if (message.isUser) {
                  return Row(
                    children: [
                      Container(
                        constraints: BoxConstraints(
                          minWidth: messageMinWidth,
                          maxWidth: messageMaxWidth,
                        ),
                        child: _OwnMessage(
                          text: message.message,
                        ),
                      ),
                      const Spacer(),
                    ],
                  );
                } else {
                  return Row(
                    children: [
                      const Spacer(),
                      Container(
                        constraints: BoxConstraints(
                          minWidth: messageMinWidth,
                          maxWidth: messageMaxWidth,
                        ),
                        child: _AssistantMessage(
                          text: message.message,
                          isLoading: message.isLoading,
                        ),
                      ),
                    ],
                  );
                }
              }),
        ),
        _MessageInputBar(
          isEnabled: isEnabled,
          onSendMessage: onSendMessage,
          onModelSelected: onModelSelected,
          selectedModel: selectedModel,
          models: models,
        ),
      ],
    );
  }
}

class _OwnMessage extends StatelessWidget {
  const _OwnMessage({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
        padding: const EdgeInsets.only(
          left: 16.0,
          right: 16.0,
          top: 16.0,
        ),
        child: Card.filled(
          elevation: 4,
          color: Theme.of(context).colorScheme.surfaceVariant,
          child: Padding(
            padding: const EdgeInsets.symmetric(
              vertical: 8.0,
              horizontal: 16.0,
            ),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.start,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  S.current.chat_you_name,
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(width: 16.0),
                MarkdownBlock(
                  config: _markdownConfig(context),
                  data: text,
                  selectable: true,
                )
              ],
            ),
          ),
        ));
  }
}

class _AssistantMessage extends StatelessWidget {
  const _AssistantMessage({
    required this.text,
    required this.isLoading,
  });

  final String text;
  final bool isLoading;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(
        left: 16.0,
        right: 16.0,
        top: 16.0,
      ),
      child: Card.outlined(
        elevation: 4,
        child: Padding(
          padding: const EdgeInsets.symmetric(
            vertical: 8.0,
            horizontal: 16.0,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              Text(
                S.current.chat_assistant_name,
                style: Theme.of(context).textTheme.titleLarge,
              ),
              MarkdownBlock(
                config: _markdownConfig(context),
                data: text,
                selectable: true,
              )
            ],
          ),
        ),
      ),
    );
  }
}

class _MessageInputBar extends StatelessWidget {
  _MessageInputBar({
    required this.isEnabled,
    required this.onSendMessage,
    required this.onModelSelected,
    required this.models,
    this.selectedModel,
  });

  final bool isEnabled;
  final Function(String) onSendMessage;
  final Function(ChatModel?) onModelSelected;
  final ChatModel? selectedModel;
  final List<ChatModel> models;

  final TextEditingController _controller = TextEditingController();
  final _focusNode = FocusNode();

  @override
  Widget build(BuildContext context) {
    final Function(ChatModel?)? dropdownCallback;
    if (isEnabled) {
      _focusNode.requestFocus();
      dropdownCallback = onModelSelected;
    } else {
      dropdownCallback = null;
    }
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Expanded(
            child: TextField(
              focusNode: _focusNode,
              controller: _controller,
              enabled: isEnabled,
              onSubmitted: onSendMessage,
              decoration: InputDecoration(
                suffixIcon: IconButton(
                  onPressed: () => {onSendMessage(_controller.value.text)},
                  icon: const Icon(Icons.send),
                ),
                border: const OutlineInputBorder(),
                hintText: S.current.chat_text_input_hint,
              ),
            ),
          ),
          const SizedBox(width: 16.0),
          DropdownButton<ChatModel>(
            hint: Text(S.current.chat_model_dropdown_hint),
            onChanged: dropdownCallback,
            value: selectedModel,
            items: models
                .map((e) => DropdownMenuItem(value: e, child: Text(e.name)))
                .toList(),
          ),
        ],
      ),
    );
  }
}

_markdownConfig(BuildContext context) {
  final isDark = Theme.of(context).brightness == Brightness.dark;
  return isDark ? MarkdownConfig.darkConfig : MarkdownConfig.defaultConfig;
}
