import 'package:olpaka/feature/chat/state.dart';

sealed class ChatSessionsEvent{ }

class ChatSessionsEventShowCreateChatDialog extends ChatSessionsEvent{
  final List<ChatModel> models;

  ChatSessionsEventShowCreateChatDialog(this.models);
}