import 'dart:async';
import 'dart:math';

import 'package:intl/intl.dart';
import 'package:olpaka/core/ollama/repository.dart';
import 'package:olpaka/core/state/model_state_holder.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class ModelsViewModel extends BaseViewModel {
  final ModelStateHolder _modelManager;

  ModelsState state = ModelsStateLoading();
  final _events = StreamController<ModelsEvent>.broadcast();

  Stream<ModelsEvent> get events => _events.stream.map((val) => val);

  ModelsViewModel(this._modelManager);

  onCreate() async {
    _modelManager.addListener(_onModelsChanged);
    final result = await _modelManager.refresh();
    switch (result) {
      case ListModelResultConnectionError():
      case ListModelResultError():
        _events.add(ModelsEventShowError(
          title: S.current.models_dialog_load_models_error_title,
          message: S.current.models_dialog_load_models_error_message,
        ));
      case ListModelsResultSuccess():
    }
  }

  onAddModelClicked() {
    _events.add(ModelsEventShowAddModelDialog());
  }

  addModel(String model) async {
    final result = await _modelManager.download(model);
    switch(result){
      case DownloadModelResponseConnectionError():
      case DownloadModelResponseError():
      _events.add(ModelsEventShowError(
        title: S.current.models_dialog_download_model_error_title,
        message: S.current.models_dialog_download_model_error_message,
      ));
      case DownloadModelResponseSuccess():
    }
  }

  removeModel(ModelItem model) async {
    final result = await _modelManager.delete(model.id);
    switch(result){
      case RemoveModelResponseConnectionError():
      case RemoveModelResponseError():
      _events.add(ModelsEventShowError(
        title: S.current.models_dialog_remove_model_error_title,
        message: S.current.models_dialog_remove_model_error_message,
      ));
      case RemoveModelResponseSuccess():
    }
  }

  _onModelsChanged() {
    final models = _modelManager.allModels;
    state = ModelsStateLoaded(models.map(_toModelItem).toList());
    notifyListeners();
  }

  ModelItem _toModelItem(ModelDomain model) {
    final String subtitle;
    if (model.isDownloaded) {
      subtitle = [
        model.size?.readableFileSize(),
        model.params,
        model.quantization,
      ].nonNulls.join(" • ");
    } else {
      subtitle = S.current.models_state_download;
    }
    return ModelItem(
      id: model.fullName,
      title: "${model.name} (${model.fullName})",
      subtitle: subtitle,
      isLoading: !model.isDownloaded,
    );
  }

  @override
  void dispose() {
    _modelManager.removeListener(_onModelsChanged);
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

class ModelsEventShowError extends ModelsEvent {
  final String title;
  final String message;

  ModelsEventShowError({
    required this.title,
    required this.message,
  });
}

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
