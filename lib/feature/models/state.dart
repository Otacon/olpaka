
sealed class ModelsState {}

class ModelsStateLoading extends ModelsState {}

class ModelsStateLoaded extends ModelsState {
  final List<ModelItem> models;

  ModelsStateLoaded(this.models);
}


class ModelItem {
  final String id;
  final String title;
  final String subtitle;
  final bool isLoading;
  final double? progress;

  ModelItem({
    required this.id,
    required this.title,
    required this.subtitle,
    required this.isLoading,
    required this.progress
  });
}