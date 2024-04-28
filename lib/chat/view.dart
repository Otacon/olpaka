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
        viewModel.onCreate();
      },
      builder: (context, viewModel, child) {
        final state = viewModel.state;
        return Scaffold(
            appBar: AppBar(
              title: const Text("Olpaka"),
              backgroundColor: Theme.of(context).colorScheme.inversePrimary,
            ),
            body: _Content(
              messages: state.messages,
              models: state.models,
              selectedModel: state.selectedModel,
              onModelSelected: (model) => {viewModel.onModelChanged(model)},
              onSendMessage: (message) => {viewModel.onSendMessage(message)},
            ));
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
    this.selectedModel,
  });

  final List<ChatMessage> messages;
  final List<String> models;
  final String? selectedModel;
  final Function(String) onSendMessage;
  final Function(String?) onModelSelected;
  final TextEditingController _controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Expanded(
          child: ListView.builder(
            itemCount: messages.length,
            itemBuilder: (context, index) {
              final message = messages[index];
              final double paddingStart;
              final double paddingEnd;
              final Color? bubbleColor;
              if (message.isUser) {
                paddingStart = 96;
                paddingEnd = 16;
                bubbleColor = Theme.of(context).colorScheme.inversePrimary;
              } else {
                paddingStart = 16;
                paddingEnd = 96;
                bubbleColor = null;
              }
              return Padding(
                padding: EdgeInsets.only(
                    left: paddingStart, right: paddingEnd, top: 16.0),
                child: Card.filled(
                  color: bubbleColor,
                  child: Padding(
                    padding: const EdgeInsets.symmetric(
                      vertical: 8.0,
                      horizontal: 16.0,
                    ),
                    child: _Message(
                      isOwn: message.isUser,
                      text: message.message,
                    ),
                  ),
                ),
              );
            },
          ),
        ),
        Padding(
          padding: const EdgeInsets.all(16.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: TextField(
                  controller: _controller,
                  onSubmitted: (message) => {onSendMessage(message)},
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
              DropdownMenu<String>(
                width: 250,
                onSelected: (model) => { onModelSelected(model) },
                label: const Text("Model"),
                initialSelection: selectedModel,
                dropdownMenuEntries: models
                    .map((e) => DropdownMenuEntry(value: e, label: e))
                    .toList(),
              ),
            ],
          ),
        )
      ],
    );
  }
}

class _Message extends StatelessWidget {
  const _Message({required this.isOwn, required this.text});

  final bool isOwn;
  final String text;

  @override
  Widget build(BuildContext context) {
    final Text sender;
    if (isOwn) {
      sender = Text(
        "You",
        style: Theme.of(context).textTheme.titleLarge,
      );
    } else {
      sender = Text(
        "Assistant",
        style: Theme.of(context).textTheme.titleLarge,
      );
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        sender,
        MarkdownBlock(
          data: text,
          selectable: true,
        )
      ],
    );
  }
}
