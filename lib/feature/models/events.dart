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

class ModelsEventShowRemoveModelDialog extends ModelsEvent {
  final String modelId;
  final String modelName;

  ModelsEventShowRemoveModelDialog(this.modelId, this.modelName);
}
