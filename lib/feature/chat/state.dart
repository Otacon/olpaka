sealed class ChatState {}

class ChatStateLoading extends ChatState { }

class ChatStateContent extends ChatState {
  final ChatModel selectedModel;
  final List<ChatModel> models;
  final List<ChatMessage> messages;
  final bool isGeneratingMessage;

  ChatStateContent({
    required this.selectedModel,
    required this.models,
    required this.messages,
    required this.isGeneratingMessage,
  });
}

class ChatStateError extends ChatState {
  final String title;
  final String message;

  ChatStateError(this.title, this.message);
}

sealed class ChatMessage{
  final String message;

  ChatMessage(this.message);
}

class ChatMessageUser extends ChatMessage{

  ChatMessageUser(super.message);

}

class ChatMessageError extends ChatMessage{
  ChatMessageError(super.message);

}

class ChatMessageAssistant extends ChatMessage{
  final bool isLoading;

  ChatMessageAssistant(super.message, {required this.isLoading});

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
