sealed class ChatEvent {}

class GenericError extends ChatEvent {
  final String title;
  final String message;

  GenericError(this.title, this.message);
}

class OllamaNotFound extends ChatEvent {
  final String title;
  final String message;
  final String positive;

  OllamaNotFound(this.title, this.message, this.positive);
}

class ModelNotFound extends ChatEvent {
  final String title;
  final String message;
  final String positive;

  ModelNotFound(this.title, this.message, this.positive);
}