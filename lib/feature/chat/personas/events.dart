import 'package:olpaka/feature/chat/conversation/state.dart';

sealed class PersonasEvent{ }

class PersonasEventShowCreateChatDialog extends PersonasEvent{
  final List<ChatModel> models;

  PersonasEventShowCreateChatDialog(this.models);
}