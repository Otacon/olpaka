import 'dart:async';

import 'package:olpaka/core/ollama/repository.dart';
import 'package:olpaka/core/state/models/model_domain.dart';
import 'package:olpaka/core/state/models/model_state_holder.dart';
import 'package:olpaka/core/utils/size_formatter.dart';
import 'package:olpaka/feature/models/events.dart';
import 'package:olpaka/feature/models/state.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class ModelsViewModel extends BaseViewModel {
  final ModelStateHolder _modelManager;

  ModelsState state = ModelsStateLoading();
  final _events = StreamController<ModelsEvent>.broadcast();

  Stream<ModelsEvent> get events => _events.stream.map((val) => val);

  ModelsViewModel(this._modelManager);

  onCreate() async {
    _modelManager.downloadingModels.addListener(_onModelsChanged);
    _modelManager.cachedModels.addListener(_onModelsChanged);
    await _loadData();
  }

  onAddModelClicked() {
    _events.add(ModelsEventShowAddModelDialog());
  }

  addModel(String model) async {
    final result = await _modelManager.download(model);
    switch (result) {
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
    switch (result) {
      case RemoveModelResponseConnectionError():
      case RemoveModelResponseError():
        _events.add(ModelsEventShowError(
          title: S.current.models_dialog_remove_model_error_title,
          message: S.current.models_dialog_remove_model_error_message,
        ));
      case RemoveModelResponseSuccess():
    }
  }

  onRefreshClicked() async {
    await _loadData();
  }

  _loadData() async {
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

  _onModelsChanged() {
    final available = _modelManager.cachedModels.value.map<ModelDomain>((e) => e).toList();
    final downloading = _modelManager.downloadingModels.value.map<ModelDomain>((e) => e).toList();
    final models = available + downloading;
    state = ModelsStateLoaded(models.map(_toModelItem).toList());
    notifyListeners();
  }

  ModelItem _toModelItem(ModelDomain model) {
    final String subtitle;
    final String name;
    final bool isLoading;
    final double? progress;
    switch (model) {
      case ModelDomainDownloading():
        name = model.name;
        isLoading = true;
        progress = model.progress;
        subtitle = S.current.models_state_download;
      case ModelDomainAvailable():
        name = "${model.friendlyName} (${model.name})";
        isLoading = false;
        progress = null;
        subtitle = [
          model.size?.readableFileSize(),
          model.params,
          model.quantization,
        ].nonNulls.join(" • ");
      case ModelDomainError():
        name = model.name;
        isLoading = true;
        progress = null;
        subtitle = "Error";
    }
    return ModelItem(
      id: model.id,
      title: name,
      subtitle: subtitle,
      isLoading: isLoading,
      progress: progress,
    );
  }

  @override
  void dispose() {
    _modelManager.cachedModels.removeListener(_onModelsChanged);
    _modelManager.downloadingModels.removeListener(_onModelsChanged);
    super.dispose();
  }
}
