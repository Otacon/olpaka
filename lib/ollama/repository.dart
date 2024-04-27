import 'dart:convert';
import 'dart:core';
import 'package:dio/dio.dart';
import 'package:olpaka/ollama/model.dart';

//TODO add error handling
class OllamaRepository {
  final Dio _client;
  final String _path = "http://localhost:11434/api";

  OllamaRepository(this._client);

  Future<List<Model>> listModels() async {

    final Response<String> response = await _client.get("$_path/tags");

    final statusCode = response.statusCode;
    final body = response.data;
    if (statusCode == null || body == null) {
      return List.empty();
    }

    if (statusCode >= 200 && statusCode <= 299) {
      final json = jsonDecode(body);
      final models =
          List<Model>.from(json["models"].map((model) => _parseModel(model)));
      return models;
    } else {
      return List.empty();
    }
  }

  Future<String> generate(String model, String prompt) async {
    final Response<String> response = await _client.post(
      "$_path/generate",
      data: {
        "model": model,
        "prompt": prompt,
        "stream": false
      },
    );
    final statusCode = response.statusCode;
    final body = response.data;
    if (statusCode == null || body == null) {
      return "Error";
    }

    if (statusCode >= 200 && statusCode <= 299) {
      final json = jsonDecode(body);
      return json["response"];
    } else {
      return "";
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
