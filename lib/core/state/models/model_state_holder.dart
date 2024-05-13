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
        cachedModels.value = ollamaModels;
      case ListModelResultConnectionError():
      case ListModelResultError():
    }
    return response;
  }

  Future<DownloadModelResponse> download(String modelName) async {
    final result = await _ollama.downloadModel(modelName);
    final Stream<DownloadChunk> stream;
    switch (result) {
      case DownloadModelStreamingResultSuccess():
        stream = result.chunkStream;
      case DownloadModelStreamingResultNotFound():
      case DownloadModelStreamingResultConnectionError():
        return DownloadModelResponseError();
    }
    final downloadingModel = ModelDomainDownloading(
      id: modelName,
      name: modelName,
    );
    _addDownloadingModel(downloadingModel);

    await for (final downloadChunk in stream) {
      switch (downloadChunk) {
        case DownloadChunkError():
          _removeDownloadingModel(modelName);
          await refresh();
          return DownloadModelResponseError();
        case DownloadChunkProgress():
          _updateDownloadingModel(modelName, downloadChunk);
      }
    }
    _removeDownloadingModel(modelName);
    await refresh();
    return DownloadModelResponseSuccess();
  }

  Future<RemoveModelResponse> delete(String modelName) async {
    //TODO map between layers
    final response = await _ollama.removeModel(modelName);
    await refresh();
    return response;
  }

  void _addDownloadingModel(ModelDomainDownloading model) {
    final downloadingPlusNew = downloadingModels.value.toList(growable: true);
    downloadingPlusNew.add(model);
    downloadingModels.value = downloadingPlusNew;
  }

  int? _getDownloadingModelIndex(String modelName) {
    var index = 0;
    for (final model in downloadingModels.value) {
      if (model.id == modelName) {
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
    final currentModels = downloadingModels.value.toList(growable: true);
    currentModels.removeAt(index);
    downloadingModels.value = currentModels;
  }

  void _updateDownloadingModel(String id, DownloadChunkProgress chunk) {
    final index = _getDownloadingModelIndex(id);
    if (index == null) {
      return;
    }
    final currentModels = downloadingModels.value.toList(growable: true);
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
    downloadingModels.value = currentModels;
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
