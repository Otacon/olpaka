import 'package:flutter/material.dart';

class ChatScreen extends StatelessWidget{
  const ChatScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Olpaka!"),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
    );
  }

}