sealed class ModelsEvent {}

class ModelsEventShowAddModelDialog extends ModelsEvent {}

class ModelsEventShowError extends ModelsEvent {
  final String title;
  final String message;

  ModelsEventShowError({
    required this.title,
    required this.message,
  });
}