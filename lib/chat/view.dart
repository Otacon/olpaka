import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
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
            ));
      },
    );
  }
}

class _Content extends StatelessWidget {
  const _Content({
    required this.messages,
    required this.models,
    this.selectedModel,
  });

  final List<ChatMessage> messages;
  final List<String> models;
  final String? selectedModel;

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
              if (message.isUser) {
                paddingStart = 48;
                paddingEnd = 16;
              } else {
                paddingStart = 16;
                paddingEnd = 48;
              }
              return Padding(
                padding: EdgeInsets.only(
                    left: paddingStart, right: paddingEnd, top: 16.0),
                child: Card.filled(
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
                  decoration: InputDecoration(
                    suffixIcon: IconButton(
                      onPressed: () => {},
                      icon: const Icon(Icons.send),
                    ),
                    border: const OutlineInputBorder(),
                    hintText: "Message Olpaka",
                  ),
                ),
              ),
              const SizedBox(width: 16.0),
              DropdownMenu<String>(
                width: 200,
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
    final CrossAxisAlignment alignment;
    if (isOwn) {
      alignment = CrossAxisAlignment.start;
      sender = Text(
        "You",
        style: Theme.of(context).textTheme.titleLarge,
      );
    } else {
      alignment = CrossAxisAlignment.end;
      sender = Text(
        "Assistant",
        style: Theme.of(context).textTheme.titleLarge,
      );
    }
    return Column(
      crossAxisAlignment: alignment,
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
