import 'dart:async';

import 'package:olpaka/core/analytics/analytics.dart';
import 'package:olpaka/core/analytics/event.dart';
import 'package:olpaka/core/analytics/screen_view.dart';
import 'package:olpaka/core/ollama/delete_model_result.dart';
import 'package:olpaka/core/ollama/list_models_result.dart';
import 'package:olpaka/core/state/models/download_model_response.dart';
import 'package:olpaka/core/state/models/model_domain.dart';
import 'package:olpaka/core/state/models/model_state_holder.dart';
import 'package:olpaka/core/utils/size_formatter.dart';
import 'package:olpaka/feature/models/events.dart';
import 'package:olpaka/feature/models/state.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class ModelsViewModel extends BaseViewModel {
  final ModelStateHolder _modelManager;
  final Analytics _analytics;

  ModelsState state = ModelsStateLoading();

  final _events = StreamController<ModelsEvent>.broadcast();
  Stream<ModelsEvent> get events => _events.stream.map((val) => val);

  ModelsViewModel(this._modelManager, this._analytics);

  onCreate() async {
    _analytics.screenView(ScreenViewModels());
    _modelManager.allModels.addListener(_onModelsChanged);
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
        _analytics.event(EventDownloadModel(model));
    }
  }

  onRemoveModel(ModelItem modelItem) async {
    _events.add(ModelsEventShowRemoveModelDialog(modelItem.id, modelItem.id));
  }

  onConfirmRemoveModel(String modelId) async {
    final result = await _modelManager.delete(modelId);
    switch (result) {
      case RemoveModelResponseConnectionError():
      case RemoveModelResponseError():
        _events.add(ModelsEventShowError(
          title: S.current.models_dialog_delete_model_error_title,
          message: S.current.models_dialog_delete_model_error_message,
        ));
      case RemoveModelResponseSuccess():
        _analytics.event(EventRemoveModel(modelId));
    }
  }

  onRefreshClicked() async {
    await _loadData();
  }

  _loadData() async {
    state = ModelsStateLoading();
    notifyListeners();
    final result = await _modelManager.refresh();
    switch (result) {
      case ListModelResultConnectionError():
      case ListModelResultError():
        state = ModelsStateError(
          S.current.error_missing_ollama_title,
          S.current.error_missing_ollama_message,
          showFab: false,
          ctaText: S.current.error_missing_ollama_positive,
        );
        notifyListeners();
      case ListModelsResultSuccess():
    }
  }

  _onModelsChanged() {
    final models = _modelManager.allModels.value;
    if (models.isEmpty) {
      state = ModelsStateError(
        S.current.models_error_no_models_title,
        S.current.models_error_no_models_message,
        showFab: true,
      );
    } else {
      state = ModelsStateContent(models.map(_toModelItem).toList());
    }
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
    _modelManager.allModels.removeListener(_onModelsChanged);
    super.dispose();
  }
}
