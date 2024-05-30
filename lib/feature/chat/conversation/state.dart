sealed class ConversationState {}

class ConversationStateLoading extends ConversationState {}

class ConversationStateContent extends ConversationState {
  final ChatModel selectedModel;
  final List<ChatModel> models;
  final List<ConversationMessage> messages;
  final bool isGeneratingMessage;

  ConversationStateContent({
    required this.selectedModel,
    required this.models,
    required this.messages,
    required this.isGeneratingMessage,
  });
}

class ConversationStateError extends ConversationState {
  final String title;
  final String message;
  final String ctaText;

  ConversationStateError(this.title, this.message, this.ctaText);
}

sealed class ConversationMessage {
  final String message;

  ConversationMessage(this.message);
}

class ConversationMessageUser extends ConversationMessage {
  ConversationMessageUser(super.message);
}

class ConversationMessageError extends ConversationMessage {
  ConversationMessageError(super.message);
}

class ConversationMessageAssistant extends ConversationMessage {
  final bool isLoading;

  ConversationMessageAssistant(super.message, {required this.isLoading});
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
