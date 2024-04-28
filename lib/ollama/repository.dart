import 'dart:convert';
import 'dart:core';
import 'package:olpaka/app/HttpClient.dart';
import 'package:olpaka/app/logger.dart';
import 'package:olpaka/ollama/model.dart';

//TODO add error handling
class OllamaRepository {
  final HttpClient _client;

  OllamaRepository(this._client);

  Future<List<Model>> listModels() async {
    logger.i("Loading models...");
    final response = await _client.get("/tags");
    switch (response) {
      case HttpResponseSuccess():
        final json = jsonDecode(response.body)["models"];
        final models =
            List<Model>.from(json.map((model) => _parseModel(model)));
        return models;
      case HttpResponseError():
        return List.empty();
      case HttpResponseOllamaNotFound():
        return List.empty();
    }
  }

  Future<String> generate(String model, String prompt) async {
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
        return json["response"];
      case HttpResponseError():
        return "Error";
      case HttpResponseOllamaNotFound():
        return "Error";
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
