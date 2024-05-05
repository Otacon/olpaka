import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:olpaka/ollama/model.dart';
import 'package:olpaka/ollama/repository.dart';

class ModelsViewModel with ChangeNotifier {
  final OllamaRepository _repository;
  ModelsState state = ModelsStateLoading();
  final _events = StreamController<ModelsEvent>.broadcast();

  Stream<ModelsEvent> get events => _events.stream.map((val) => val);

  ModelsViewModel(this._repository);

  List<ModelItem> _models = List<ModelItem>.empty(growable: true);

  onCreate() async {
    state = ModelsStateLoading();
    notifyListeners();
    final response = await _repository.listModels();
    switch (response) {
      case ListModelsResultSuccess():
        final updatedModels = response.models.toModelItems();
        _models = updatedModels;
        state = ModelsStateLoaded(updatedModels);
        notifyListeners();
      case ListModelResultConnectionError():
      case ListModelResultError():
    }
  }

  onAddModelClicked() {
    _events.add(ModelsEventShowAddModelDialog());
  }

  addModel(String model) async {
    _models.add(ModelItem(name: model, fullName: model, isLoading: true));
    state = ModelsStateLoaded(_models);
    notifyListeners();
    await _repository.addModel(model);
    final response = await _repository.listModels();
    switch (response) {
      case ListModelsResultSuccess():
        final updatedModels = response.models.toModelItems();
        _models = updatedModels;
        state = ModelsStateLoaded(updatedModels);
        notifyListeners();
      case ListModelResultConnectionError():
      case ListModelResultError():
    }
  }

  removeModel(String model) async {
    await _repository.removeModel(model);
    final response = await _repository.listModels();
    switch (response) {
      case ListModelsResultSuccess():
        final updatedModels = response.models.toModelItems();
        _models = updatedModels;
        state = ModelsStateLoaded(updatedModels);
        notifyListeners();
      case ListModelResultConnectionError():
      case ListModelResultError():
    }
  }

}

extension Mappings on List<Model> {
  List<ModelItem> toModelItems(){
    return map((_toModelItem)).toList();
  }

  ModelItem _toModelItem(Model model) {
    return ModelItem(
      name: model.name,
      fullName: model.model,
      isLoading: false,
      size: model.size?.toString(),
    );
  }
}

sealed class ModelsState {}

class ModelsStateLoading extends ModelsState {}

class ModelsStateLoaded extends ModelsState {
  final List<ModelItem> models;

  ModelsStateLoaded(this.models);
}

sealed class ModelsEvent {}

class ModelsEventShowAddModelDialog extends ModelsEvent {}

class ModelItem {
  final String name;
  final String fullName;
  final bool isLoading;
  final String? params;
  final String? size;
  final String? something;

  ModelItem({
    required this.name,
    required this.fullName,
    required this.isLoading,
    this.params,
    this.size,
    this.something,
  });
}
