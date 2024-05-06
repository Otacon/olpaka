
import 'dart:async';

import 'package:olpaka/ollama/model.dart';
import 'package:olpaka/ollama/repository.dart';

class ModelManager{

  final OllamaRepository _ollama;

  StreamController<List<ModelDomain>> models = StreamController.broadcast();

  List<ModelDomain> cachedModels = List.empty(growable: true);
  List<ModelDomain> downloadingModels = List.empty(growable: true);

  ModelManager(this._ollama);

  Future<ListModelsResult> refresh() async {
    final response = await _ollama.listModels();
    switch(response){
      case ListModelsResultSuccess():
        final ollamaModels = response.models.map((e) => _toDomain(e, true)).toList();
        cachedModels = ollamaModels;
        _broadcastUpdate();
      case ListModelResultConnectionError():
      case ListModelResultError():
    }
    return response;
  }

  Future<DownloadModelResponse> download(String modelName) async {
    final downloadingModel = ModelDomain(name: modelName, fullName: modelName, isDownloaded: false);
    downloadingModels.add(downloadingModel);
    _broadcastUpdate();
    final response = await _ollama.downloadModel(modelName);
    downloadingModels.remove(downloadingModel);
    await refresh();
    return response;
  }

  Future<RemoveModelResponse> delete(String modelName) async {
    final response = await _ollama.removeModel(modelName);
    await refresh();
    return response;
  }

  _broadcastUpdate() {
    models.add(cachedModels + downloadingModels);
  }

  ModelDomain _toDomain(Model model, isDownloaded) {
    return ModelDomain(
        name: model.name,
        fullName: model.model,
        isDownloaded: isDownloaded,
        size: model.size,
        params: model.parameterSize,
        quantization: model.quantizationLevel
    );
  }

}

class ModelDomain{
  final String name;
  final String fullName;
  final bool isDownloaded;
  final String? params;
  final int? size;
  final String? quantization;

  ModelDomain({
    required this.name,
    required this.fullName,
    required this.isDownloaded,
    this.params,
    this.size,
    this.quantization,
  });

}