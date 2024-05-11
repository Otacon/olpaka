import 'dart:async';
import 'dart:convert';
import 'dart:core';
import 'package:olpaka/core/http_client.dart';
import 'package:olpaka/core/logger.dart';

import 'model.dart';

class OllamaRepository {
  final HttpClient _client;

  OllamaRepository(this._client);

  Future<ListModelsResult> listModels() async {
    logger.i("Loading models...");
    final response = await _client.get("/tags");
    switch (response) {
      case HttpResponseSuccess():
        final json = jsonDecode(response.body)["models"];
        final models =
            List<Model>.from(json.map((model) => _parseModel(model)));
        return ListModelsResultSuccess(models);
      case HttpResponseError():
      case HttpResponseUnknownError():
        return ListModelResultError();
      case HttpResponseConnectionError():
        return ListModelResultConnectionError();
    }
  }

  Future<RemoveModelResponse> removeModel(String model) async {
    logger.i("Removing model $model");
    final response = await _client.delete("/delete", data: {
      "model": model,
    });
    switch (response) {
      case HttpResponseSuccess():
        return RemoveModelResponseSuccess();
      case HttpResponseConnectionError():
        return RemoveModelResponseConnectionError();
      case HttpResponseError():
      case HttpResponseUnknownError():
        return RemoveModelResponseError();
    }
  }

  Future<DownloadModelResponse> downloadModel(String model) async {
    logger.i("adding model $model");
    final response =
        await _client.post("/pull", data: {"name": model, "stream": false});
    switch (response) {
      case HttpResponseSuccess():
        return DownloadModelResponseSuccess();
      case HttpResponseConnectionError():
        return DownloadModelResponseConnectionError();
      case HttpResponseError():
      case HttpResponseUnknownError():
        return DownloadModelResponseError();
    }
  }

  Future<GenerateResult> generate(String model, String prompt) async {
    logger.i("Generating answer...");
    final response = await _client.post(
      "/generate",
      data: {
        "model": model,
        "prompt": prompt,
        "stream": false,
      },
    );
    switch (response) {
      case HttpResponseSuccess():
        final json = jsonDecode(response.body);
        return GenerateResultSuccess(json["response"]);
      case HttpResponseError():
      case HttpResponseConnectionError():
      case HttpResponseUnknownError():
        return GenerateResultError();
    }
  }

  Stream<GenerateStreamingResult> generateStream(String model, String prompt) async* {
    logger.i("Generating answer...");
    yield* _client.postStreaming2(
      "/generate",
      data: {
        "model": model,
        "prompt": prompt,
        "stream": true,
      },
    ).transform(StreamTransformer.fromHandlers(
      handleData: (data, sink) {
        sink.add(_mapSomething(data));
      }
    ));
  }

  GenerateStreamingResult _mapSomething(String data) {
    final payload = jsonDecode(data);
    if(payload["done"]){
      final contextInts = List<int>.from(payload["context"]);
      return GenerateStreamingResultComplete(contextInts);
    } else {
      return GenerateStreamingResultChunk(payload["response"]);
    }
  }

  Model _parseModel(dynamic json) {
    return Model(
      name: json["name"].split(':')[0],
      model: json["model"],
      modifiedAt: json["modified_at"],
      size: json["size"],
      digest: json["digest"],
      parentModel: json["details"]["parent_model"],
      format: json["details"]["format"],
      family: json["details"]["family"],
      families: List.empty(),
      parameterSize: json["details"]["parameter_size"],
      quantizationLevel: json["details"]["quantization_level"],
    );
  }
}

sealed class ListModelsResult {}

class ListModelsResultSuccess extends ListModelsResult {
  final List<Model> models;

  ListModelsResultSuccess(this.models);
}

class ListModelResultConnectionError extends ListModelsResult {}

class ListModelResultError extends ListModelsResult {}

sealed class GenerateResult {}

class GenerateResultSuccess extends GenerateResult {
  final String answer;

  GenerateResultSuccess(this.answer);
}

sealed class GenerateStreamingResult{}

class GenerateStreamingResultChunk extends GenerateStreamingResult{
  final String chunk;

  GenerateStreamingResultChunk(this.chunk);
}

class GenerateStreamingResultComplete extends GenerateStreamingResult{
  final List<int> context;

  GenerateStreamingResultComplete(this.context);
}

class GenerateResultError extends GenerateResult {}

sealed class DownloadModelResponse {}

class DownloadModelResponseSuccess extends DownloadModelResponse {}

class DownloadModelResponseConnectionError extends DownloadModelResponse {}

class DownloadModelResponseError extends DownloadModelResponse {}

sealed class RemoveModelResponse {}

class RemoveModelResponseSuccess extends RemoveModelResponse {}

class RemoveModelResponseConnectionError extends RemoveModelResponse {}

class RemoveModelResponseError extends RemoveModelResponse {}
