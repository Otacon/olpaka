class ChatSessionsState {
  final List<ChatSession> chats;

  ChatSessionsState(this.chats);
}

class ChatSession {
  final String id;
  final String model;

  ChatSession(this.id, this.model);
}