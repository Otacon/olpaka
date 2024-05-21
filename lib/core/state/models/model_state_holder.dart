import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:olpaka/core/ollama/delete_model_result.dart';
import 'package:olpaka/core/ollama/download_streaming_result.dart';
import 'package:olpaka/core/ollama/list_models_result.dart';
import 'package:olpaka/core/ollama/model.dart';
import 'package:olpaka/core/ollama/repository.dart';
import 'package:olpaka/core/state/models/download_model_response.dart';
import 'package:olpaka/core/state/models/model_domain.dart';

class ModelStateHolder {
  final OllamaRepository _ollama;

  final allModels = ValueNotifier<List<ModelDomain>>(List.empty());
  final cachedModels = ValueNotifier<List<ModelDomainAvailable>>(List.empty());
  final downloadingModels =
      ValueNotifier<List<ModelDomainDownloading>>(List.empty());

  ModelStateHolder(this._ollama);

  Future<ListModelsResult> refresh() async {
    //TODO this shouldn't be pass-through.
    final response = await _ollama.listModels();
    switch (response) {
      case ListModelsResultSuccess():
        final ollamaModels = response.models.map(_toDomainAvailable).toList();
        _updateAvailableModels(ollamaModels);
      case ListModelResultConnectionError():
      case ListModelResultError():
    }
    return response;
  }

  Future<DownloadModelResponse> download(String modelName) async {
    final modelId = modelName.toModelId();
    final result = await _ollama.downloadModel(modelId);
    final Stream<DownloadChunk> stream;
    switch (result) {
      case DownloadModelStreamingResultSuccess():
        stream = result.chunkStream;
      case DownloadModelStreamingResultNotFound():
      case DownloadModelStreamingResultConnectionError():
        return DownloadModelResponseError();
    }
    final downloadingModel = ModelDomainDownloading(
      id: modelId,
      name: modelId,
    );
    _addDownloadingModel(downloadingModel);

    await for (final downloadChunk in stream) {
      switch (downloadChunk) {
        case DownloadChunkError():
          _removeDownloadingModel(modelId);
          await refresh();
          return DownloadModelResponseError();
        case DownloadChunkProgress():
          _updateDownloadingModel(modelId, downloadChunk);
      }
    }
    _removeDownloadingModel(modelId);
    await refresh();
    return DownloadModelResponseSuccess();
  }

  Future<RemoveModelResponse> delete(String modelName) async {
    final modelId = modelName.toModelId();
    //TODO map between layers
    final response = await _ollama.removeModel(modelId);
    await refresh();
    return response;
  }

  void _addDownloadingModel(ModelDomain model) {
    final downloadingPlusNew = _getAllModels();
    final existingModelIndex = _getDownloadingModelIndex(model.id);
    if(existingModelIndex == null) {
      downloadingPlusNew.add(model);
      _updateModels(downloadingPlusNew);
    } else {
      downloadingPlusNew[existingModelIndex] = ModelDomainDownloading(id: model.id, name: model.name);
      _updateModels(downloadingPlusNew);
    }
  }

  int? _getDownloadingModelIndex(String modelId) {
    var index = 0;
    for (final model in _getAllModels()) {
      if (model.id == modelId) {
        return index;
      }
      index++;
    }
    return null;
  }

  void _removeDownloadingModel(String id) {
    final index = _getDownloadingModelIndex(id);
    if (index == null) {
      return;
    }
    final currentModels = _getAllModels();
    currentModels.removeAt(index);
    _updateModels(currentModels);
  }

  void _updateDownloadingModel(String id, DownloadChunkProgress chunk) {
    final index = _getDownloadingModelIndex(id);
    if (index == null) {
      return;
    }
    final currentModels = _getAllModels();
    final downloadingModel = currentModels.elementAt(index);
    currentModels.remove(downloadingModel);

    double? progress;
    final completed = chunk.completed;
    final total = chunk.total;
    if (completed != null && total != null) {
      progress = completed / total;
    }

    final updatedModel = ModelDomainDownloading(
      id: downloadingModel.id,
      name: downloadingModel.name,
      status: chunk.status,
      progress: progress,
    );

    currentModels.insert(index, updatedModel);
    _updateModels(currentModels);
  }

  void _updateModels(List<ModelDomain> models) {
    cachedModels.value = models.whereType<ModelDomainAvailable>().toList();
    downloadingModels.value =
        models.whereType<ModelDomainDownloading>().toList();
    allModels.value = models;
  }

  void _updateAvailableModels(List<ModelDomainAvailable> models) {
    final pendingModels = downloadingModels.value.toList();
    final updatedModels = List<ModelDomain>.empty(growable: true);
    updatedModels.addAll(models);
    updatedModels.addAll(pendingModels);
    _updateModels(updatedModels);
  }

  List<ModelDomain> _getAllModels() {
    return allModels.value.map<ModelDomain>((e) => e).toList(growable: true);
  }

  ModelDomainAvailable _toDomainAvailable(Model model) {
    return ModelDomainAvailable(
        id: model.model,
        name: model.model,
        friendlyName: model.name,
        size: model.size,
        params: model.parameterSize,
        quantization: model.quantizationLevel);
  }
}

extension ModelNameFixer on String {
  String toModelId() {
    if (contains(":")) {
      return this;
    } else {
      return "$this:latest";
    }
  }
}
