import 'package:flutter/material.dart';
import 'package:olpaka/feature/chat/conversation/view.dart';
import 'package:olpaka/feature/chat/personas/view.dart';
import 'package:olpaka/generated/l10n.dart';

class ChatScreen extends StatelessWidget {
  const ChatScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        elevation: 4,
        shadowColor: Theme.of(context).colorScheme.shadow,
        centerTitle: true,
        title: Text(
          S.current.app_name,
          style: Theme.of(context).textTheme.headlineMedium,
        ),
      ),
      body: Row(
        children: [
          const Expanded(flex: 1, child: PersonasScreen()),
          const VerticalDivider(),
          Expanded(flex: 3, child: ConversationScreen()),
        ],
      ),
    );
  }
}
