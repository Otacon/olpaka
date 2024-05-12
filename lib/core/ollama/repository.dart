import 'dart:convert';
import 'dart:core';

import 'package:olpaka/core/http_client/http_client.dart';
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

  Future<DownloadModelStreamingResult> downloadModel(String model) async {
    final payload = {"name": model, "stream": true};
    final response = await _client.postStreaming("/pull", data: payload);
    switch (response) {
      case HttpStreamingResponseSuccess():
        final stream = response.chunks.map((chunk) {
          final chunkJson = jsonDecode(chunk);
          return DownloadChunk(
            status: chunkJson["status"],
            digest: chunkJson["digest"],
            total: chunkJson["total"],
            completed: chunkJson["completed"],
          );
        });
        return DownloadModelStreamingResultSuccess(stream);
      case HttpStreamingResponseError():
        if (response.code == 404) {
          return DownloadModelStreamingResultNotFound();
        } else {
          return DownloadModelStreamingResultConnectionError();
        }
      case HttpStreamingResponseConnectionError():
      case HttpStreamingResponseUnknownError():
        return DownloadModelStreamingResultConnectionError();
    }
  }

  Future<GenerateStreamingResult> generate(String model, String prompt) async {
    logger.i("Generating answer...");
    final payload = {
      "model": model,
      "prompt": prompt,
      "stream": true,
    };
    final response = await _client.postStreaming("/generate", data: payload);
    switch (response) {
      case HttpStreamingResponseSuccess():
        final stream = response.chunks.map(_mapGenerateChunk);
        return GenerateStreamingResultSuccess(stream);
      case HttpStreamingResponseError():
      case HttpStreamingResponseConnectionError():
      case HttpStreamingResponseUnknownError():
        return GenerateStreamingResultError();
    }
  }

  GenerateChunk _mapGenerateChunk(String data) {
    final payload = jsonDecode(data);
    final contextJson = payload["context"];
    final List<int>? context;
    if (contextJson != null) {
      context = List<int>.from(contextJson);
    } else {
      context = null;
    }
    final String message = payload["response"];
    final bool done = payload["done"];
    return GenerateChunk(message, context, done);
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

class GenerateResultError extends GenerateResult {}

sealed class GenerateStreamingResult {}

class GenerateStreamingResultSuccess extends GenerateStreamingResult {
  final Stream<GenerateChunk> chunkStream;

  GenerateStreamingResultSuccess(this.chunkStream);
}

class GenerateStreamingResultConnectionError extends GenerateStreamingResult {}

class GenerateStreamingResultError extends GenerateStreamingResult {}

class GenerateChunk {
  final String message;
  final List<int>? context;
  final bool done;

  GenerateChunk(this.message, this.context, this.done);
}

sealed class DownloadModelStreamingResult {}

class DownloadModelStreamingResultSuccess extends DownloadModelStreamingResult {
  final Stream<DownloadChunk> chunkStream;

  DownloadModelStreamingResultSuccess(this.chunkStream);
}

class DownloadModelStreamingResultNotFound
    extends DownloadModelStreamingResult {}

class DownloadModelStreamingResultConnectionError
    extends DownloadModelStreamingResult {}

class DownloadChunk {
  final String status;
  final String? digest;
  final int? total;
  final int? completed;

  DownloadChunk(
      {required this.status,
      required this.digest,
      required this.total,
      required this.completed});
}

sealed class DownloadModelResponse {}

class DownloadModelResponseSuccess extends DownloadModelResponse {}

class DownloadModelResponseConnectionError extends DownloadModelResponse {}

class DownloadModelResponseError extends DownloadModelResponse {}

sealed class RemoveModelResponse {}

class RemoveModelResponseSuccess extends RemoveModelResponse {}

class RemoveModelResponseConnectionError extends RemoveModelResponse {}

class RemoveModelResponseError extends RemoveModelResponse {}
