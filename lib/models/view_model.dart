import 'dart:async';
import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:intl/intl.dart';
import 'package:olpaka/ollama/model_manager.dart';

class ModelsViewModel with ChangeNotifier {
  final ModelManager _repository;
  ModelsState state = ModelsStateLoading();
  final _events = StreamController<ModelsEvent>.broadcast();

  Stream<ModelsEvent> get events => _events.stream.map((val) => val);

  ModelsViewModel(this._repository);

  late StreamSubscription _subscription;

  onCreate() async {
    _subscription = _repository.models.stream.listen(_onModelsChanged);
    _repository.refresh();
  }

  onAddModelClicked() {
    _events.add(ModelsEventShowAddModelDialog());
  }

  addModel(String model) async {
    await _repository.download(model);
  }

  removeModel(String model) async {
    await _repository.delete(model);
  }

  _onModelsChanged(List<ModelDomain> models) {
    state = ModelsStateLoaded(models.map(_toModelItem).toList());
    notifyListeners();
  }

  ModelItem _toModelItem(ModelDomain model) {
    final subtitle = [
      model.size?.readableFileSize(),
      model.params,
      model.quantization,
    ].nonNulls.join(" • ");
    return ModelItem(
      id: model.name,
      title: "${model.name} (${model.fullName})",
      subtitle: subtitle,
      isLoading: !model.isDownloaded,
    );
  }

  @override
  void dispose() {
    _subscription.cancel();
    super.dispose();
  }
}

extension FileFormatter on num {
  String readableFileSize() {
    if (this <= 0) return "0";

    const base = 1000;
    final units = ["B", "kB", "MB", "GB", "TB"];

    int digitGroups = (log(this) / log(base)).floor();
    return "${NumberFormat("#,##0.#").format(this / pow(base, digitGroups))} ${units[digitGroups]}";
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
  final String id;
  final String title;
  final String subtitle;
  final bool isLoading;

  ModelItem({
    required this.id,
    required this.title,
    required this.subtitle,
    required this.isLoading,
  });
}
