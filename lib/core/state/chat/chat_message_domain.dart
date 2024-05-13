sealed class ChatMessageDomain {}

class ChatMessageUserDomain extends ChatMessageDomain {
  final String message;

  ChatMessageUserDomain(this.message);
}

class ChatMessageAssistantDomain extends ChatMessageDomain {
  final String message;
  final bool isFinalised;

  ChatMessageAssistantDomain(this.message, this.isFinalised);
}

class ChatMessageErrorDomain extends ChatMessageDomain {
}
