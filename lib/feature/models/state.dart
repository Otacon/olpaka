sealed class ModelsState {}

class ModelsStateLoading extends ModelsState {}

class ModelsStateContent extends ModelsState {
  final List<ModelItem> models;

  ModelsStateContent(this.models);
}

class ModelsStateError extends ModelsState {
  final String title;
  final String message;
  final String? ctaText;
  final bool showFab;

  ModelsStateError(this.title, this.message, {required this.showFab, this.ctaText});
}

class ModelItem {
  final String id;
  final String title;
  final String subtitle;
  final bool isLoading;
  final double? progress;

  ModelItem(
      {required this.id,
      required this.title,
      required this.subtitle,
      required this.isLoading,
      required this.progress});
}
