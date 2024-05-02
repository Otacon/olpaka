import 'dart:convert';
import 'dart:core';
import 'package:olpaka/app/http_client.dart';
import 'package:olpaka/app/logger.dart';
import 'package:olpaka/ollama/model.dart';

class OllamaRepository {
  final HttpClient _client;

  OllamaRepository(this._client);

  Future<ListModelsResult> listModels() async {
    logger.i("Loading models...");
    final response = await _client.get("/tags");
    switch (response) {
      case HttpResponseSuccess():
        final json = jsonDecode(response.body)["models"];
        final models = List<Model>.from(json.map((model) => _parseModel(model)));
        return ListModelsResultSuccess(models);
      case HttpResponseError():
      case HttpResponseUnknownError():
        return ListModelResultError();
      case HttpResponseConnectionError():
        return ListModelResultConnectionError();
      case HttpResponseCorsError():
        return ListModelResultCorsError();
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
      case HttpResponseCorsError():
        return GenerateResultError();
    }
  }

  Model _parseModel(dynamic json) {
    return Model(
      name: json["name"],
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

class ListModelsResultSuccess extends ListModelsResult{
  final List<Model> models;

  ListModelsResultSuccess(this.models);

}

class ListModelResultConnectionError extends ListModelsResult{}

class ListModelResultCorsError extends ListModelsResult{}

class ListModelResultError extends ListModelsResult{}



sealed class GenerateResult {}

class GenerateResultSuccess extends GenerateResult{
  final String answer;

  GenerateResultSuccess(this.answer);

}

class GenerateResultError extends GenerateResult{}
