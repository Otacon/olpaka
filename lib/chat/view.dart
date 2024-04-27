import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
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
        return Scaffold(
          appBar: AppBar(
            title: const Text("Olpaka"),
            backgroundColor: Theme
                .of(context)
                .colorScheme
                .inversePrimary,
          ),
        );
      },
    );
  }
}