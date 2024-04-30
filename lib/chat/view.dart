import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:markdown_widget/markdown_widget.dart';
import 'package:olpaka/chat/view_model.dart';
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
              showErrorDialog(
                  context: context,
                  title: event.title,
                  message: event.message,
                  positive: "OK",
                  positiveAction: () => {});
            case OllamaNotFound():
              showErrorDialog(
                  context: context,
                  title: event.title,
                  message: event.message,
                  positive: event.positive,
                  positiveAction: () => {viewModel.onRefresh()});
            case ModelNotFound():
              showErrorDialog(
                  context: context,
                  title: event.title,
                  message: event.message,
                  positive: event.positive,
                  positiveAction: () => {viewModel.onRefresh()});
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
              "Olpaka",
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
}

showErrorDialog(
    {required BuildContext context,
    required String title,
    required String message,
    required String positive,
    required Function() positiveAction,
    String? negative,
    Function()? negativeAction}) {
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

class _Content extends StatelessWidget {
  const _Content({
    required this.messages,
    required this.models,
    required this.onSendMessage,
    required this.onModelSelected,
    this.isEnabled = true,
    this.selectedModel,
  });

  final List<ChatMessage> messages;
  final List<String> models;
  final String? selectedModel;
  final bool isEnabled;
  final Function(String) onSendMessage;
  final Function(String?) onModelSelected;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Expanded(
          child: ListView.builder(
              itemCount: messages.length,
              itemBuilder: (context, index) {
                final message = messages[index];
                if (message.isUser) {
                  return _OwnMessage(text: message.message);
                } else {
                  return _AssistantMessage(
                    text: message.message,
                    isLoading: message.isLoading,
                  );
                }
              }),
        ),
        _BottomBar(
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
        right: 64.0,
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
                "You",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(width: 16.0),
              MarkdownBlock(
                config: MarkdownConfig.darkConfig,
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
        left: 64.0,
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
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Text("Assistant",
                      style: Theme.of(context).textTheme.titleLarge),
                  const SizedBox(width: 16.0),
                  if (isLoading)
                    const SizedBox(
                      width: 16.0,
                      height: 16.0,
                      child: CircularProgressIndicator(),
                    )
                ],
              ),
              MarkdownBlock(
                config: MarkdownConfig.darkConfig,
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

class _BottomBar extends StatelessWidget {
  _BottomBar({
    required this.isEnabled,
    required this.onSendMessage,
    required this.onModelSelected,
    required this.models,
    this.selectedModel,
  });

  final bool isEnabled;
  final Function(String) onSendMessage;
  final Function(String?) onModelSelected;
  final String? selectedModel;
  final List<String> models;

  final TextEditingController _controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    final Function(String?)? dropdownCallback;
    if (isEnabled) {
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
              controller: _controller,
              enabled: isEnabled,
              onSubmitted: onSendMessage,
              decoration: InputDecoration(
                suffixIcon: IconButton(
                  onPressed: () => {onSendMessage(_controller.value.text)},
                  icon: const Icon(Icons.send),
                ),
                border: const OutlineInputBorder(),
                hintText: "Message Olpaka",
              ),
            ),
          ),
          const SizedBox(width: 16.0),
          DropdownButton<String>(
            hint: const Text("Selected model"),
            onChanged: dropdownCallback,
            value: selectedModel,
            items: models
                .map((e) => DropdownMenuItem(value: e, child: Text(e)))
                .toList(),
          ),
        ],
      ),
    );
  }
}
