import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/feature/chat/state.dart';
import 'package:olpaka/feature/chat_sessions/events.dart';
import 'package:olpaka/feature/chat_sessions/state.dart';
import 'package:olpaka/feature/chat_sessions/view_model.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class ChatSessionsScreen extends StatelessWidget {
  const ChatSessionsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<ChatSessionsViewModel>.reactive(
      viewModelBuilder: () => GetIt.I.get(),
      onViewModelReady: (viewModel) {
        viewModel.events.listen((event) {
          switch (event) {
            case ChatSessionsEventShowCreateChatDialog():
              _showCreateChatDialog(
                context: context,
                models: event.models,
                positiveAction: (modelName) =>
                    viewModel.onNewConversation(modelName),
              );
          }
        });
        viewModel.onCreate();
      },
      builder: (context, viewModel, child) {
        final items = viewModel.state.chats;
        return Stack(children: [
          _conversationList(items),
          Align(
            alignment: Alignment.bottomRight,
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: FloatingActionButton(
                onPressed: viewModel.onCreateChatClicked,
                child: const Icon(Icons.add),
              ),
            ),
          )
        ]);
      },
    );
  }

  Widget _conversationList(List<ChatSession> items) {
    return ListView.builder(
      itemCount: items.length,
      itemBuilder: (context, index) {
        final item = items[index];
        return ListTile(
          title: Text(item.id),
          subtitle: Text(item.model),
          onTap: () {},
        );
      },
    );
  }

  _showCreateChatDialog({
    required BuildContext context,
    required List<ChatModel> models,
    required Function(String) positiveAction,
  }) {
    return showDialog(
      barrierDismissible: false,
      context: context,
      builder: (context) {
        return _CreateChatDialog(
          models: models,
          positiveAction: positiveAction,
        );
      },
    );
  }
}

class _CreateChatDialog extends StatefulWidget {
  final List<ChatModel> models;
  final Function(String) positiveAction;

  const _CreateChatDialog({
    required this.models,
    required this.positiveAction,
  });

  @override
  State<_CreateChatDialog> createState() {
    return _CreateChatDialogState();
  }
}

class _CreateChatDialogState extends State<_CreateChatDialog> {
  ChatModel? selectedModel;

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text("Star a new chat"),
      content: DropdownButton<ChatModel>(
        hint: Text(S.current.chat_model_dropdown_hint),
        value: selectedModel,
        onChanged: (chatModel) {
          setState(() {
            selectedModel = chatModel;
          });
        },
        items: widget.models
            .map((e) => DropdownMenuItem(value: e, child: Text(e.name)))
            .toList(),
      ),
      actions: [
        TextButton(
          onPressed: () {
            Navigator.of(context).pop(false);
            final modelToCreate = selectedModel;
            if (modelToCreate != null) {
              widget.positiveAction(modelToCreate.id);
            }
          },
          child: const Text("Ok"),
        ),
      ],
    );
  }
}
