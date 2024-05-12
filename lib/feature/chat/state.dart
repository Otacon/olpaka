
class ChatState {
  final bool isLoading;
  final ChatModel? selectedModel;
  final List<ChatModel> models;
  final List<ChatMessage> messages;

  ChatState({
    required this.isLoading,
    required this.selectedModel,
    required this.models,
    required this.messages,
  });
}

class ChatMessage {
  final bool isUser;
  final String message;
  final bool isLoading;

  ChatMessage({
    required this.isUser,
    this.message = "",
    this.isLoading = false,
  });
}

class ChatModel {
  final String id;
  final String name;

  ChatModel(this.id, this.name);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
          other is ChatModel &&
              runtimeType == other.runtimeType &&
              id == other.id &&
              name == other.name;

  @override
  int get hashCode => id.hashCode ^ name.hashCode;
}