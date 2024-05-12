import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:olpaka/core/ollama/model.dart';
import 'package:olpaka/core/ollama/repository.dart';

class ModelStateHolder with ChangeNotifier {
  final OllamaRepository _ollama;

  Set<ModelDomain> cachedModels = <ModelDomain>{};
  List<ModelDomain> downloadingModels = List.empty(growable: true);

  Set<ModelDomain> get allModels =>
      (cachedModels.toList() + downloadingModels.toList()).toSet();

  ModelStateHolder(this._ollama);

  Future<ListModelsResult> refresh() async {
    final response = await _ollama.listModels();
    switch (response) {
      case ListModelsResultSuccess():
        final ollamaModels =
            response.models.map((e) => _toDomain(e, true)).toSet();
        cachedModels = ollamaModels;
        notifyListeners();
      case ListModelResultConnectionError():
      case ListModelResultError():
    }
    return response;
  }

  Future<DownloadModelResponse> download(String modelName) async {
    final result = await _ollama.downloadModel(modelName);
    final Stream<DownloadChunk> stream;
    switch(result){
      case DownloadModelStreamingResultSuccess():
        stream = result.chunkStream;
      case DownloadModelStreamingResultNotFound():
      case DownloadModelStreamingResultConnectionError():
        return DownloadModelResponseError();
    }
    final downloadingModel = ModelDomain(name: modelName, fullName: modelName, isDownloaded: false);
    downloadingModels.add(downloadingModel);
    notifyListeners();

    await for (final downloadResult in stream){
      final index = _findDownloadingModel(modelName);
      if (index == null) {
        continue;
      }
      final downloadingModel = downloadingModels.elementAt(index);
      downloadingModels.remove(downloadingModel);
      double? progress;
      final completed = downloadResult.completed;
      var total = downloadResult.total;
      if (completed != null && total != null) {
        progress = completed / total;
      }
      final updatedModel = ModelDomain(
        name: modelName,
        fullName: modelName,
        isDownloaded: false,
        progress: progress,
      );
      downloadingModels.insert(index, updatedModel);
      notifyListeners();
    }
    final index = _findDownloadingModel(modelName);
    if(index != null) {
      downloadingModels.removeAt(index);
    }
    await refresh();
    return DownloadModelResponseSuccess();
  }

  Future<RemoveModelResponse> delete(String modelName) async {
    final response = await _ollama.removeModel(modelName);
    await refresh();
    return response;
  }

  int? _findDownloadingModel(String modelName) {
    var index = 0;
    for (final model in downloadingModels) {
      if (model.fullName == modelName) {
        return index;
      }
      index++;
    }
    return null;
  }

  ModelDomain _toDomain(Model model, isDownloaded) {
    return ModelDomain(
        name: model.name,
        fullName: model.model,
        isDownloaded: isDownloaded,
        size: model.size,
        params: model.parameterSize,
        quantization: model.quantizationLevel);
  }
}

class ModelDomain {
  final String name;
  final String fullName;
  final bool isDownloaded;
  final String? params;
  final int? size;
  final String? quantization;
  final double? progress;

  ModelDomain(
      {required this.name,
      required this.fullName,
      required this.isDownloaded,
      this.params,
      this.size,
      this.quantization,
      this.progress});
}
