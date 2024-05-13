
import 'package:olpaka/core/ollama/model.dart';

sealed class ListModelsResult {}

class ListModelsResultSuccess extends ListModelsResult {
  final List<Model> models;

  ListModelsResultSuccess(this.models);
}

class ListModelResultConnectionError extends ListModelsResult {}

class ListModelResultError extends ListModelsResult {}