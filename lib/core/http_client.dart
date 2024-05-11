import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:fetch_client/fetch_client.dart';
import 'package:http/http.dart' as Http;

class HttpClient {
  final Dio _client;

  HttpClient(this._client);

  Future<HttpResponse> post(String endpoint, {required Object? data}) async {
    Response<String> response;
    try {
      response = await _client.post(endpoint, data: data);
      return _handleResponse(response);
    } on DioException catch (e) {
      return _handleException(e);
    }
  }

  Stream<String> postStreaming(String endpoint,
      {required Object? data}) async* {
    StreamTransformer<Uint8List, List<int>> unit8Transformer =
        StreamTransformer.fromHandlers(
      handleData: (data, sink) {
        sink.add(List<int>.from(data));
      },
    );
    try {
      final response = await _client.post<ResponseBody>(
        endpoint,
        data: data,
        options: Options(
          responseType: ResponseType.stream,
        ),
      );
      response.data?.stream
          .transform(unit8Transformer)
          .transform(const Utf8Decoder())
          .transform(const LineSplitter())
          .listen((event) {print("Received $event at ${DateTime.now().millisecond}");});
    } on DioException catch (e) {
      print(e.stackTrace);
    }
  }

  Stream<String> postStreaming2(String endpoint, {required Object? data}) async* {
    final client = FetchClient(mode: RequestMode.cors);
    final uri = Uri.http("localhost:11434", "/api/generate");
    final request = Http.Request("POST", uri);
    request.body = json.encode(data);
    final response = await client.send(request);
    yield* response.stream
        .transform(const Utf8Decoder())
        .transform(const LineSplitter());
  }

  Future<HttpResponse> delete(String endpoint, {required Object? data}) async {
    try {
      Response<String> response = await _client.delete(endpoint, data: data);
      return _handleResponse(response);
    } on DioException catch (e) {
      return _handleException(e);
    }
  }

  Future<HttpResponse> get(String endpoint) async {
    try {
      Response<String> response = await _client.get(endpoint);
      return _handleResponse(response);
    } on DioException catch (e) {
      return _handleException(e);
    }
  }

  HttpResponse _handleResponse(Response<String> response) {
    final int? statusCode = response.statusCode;
    final String? data = response.data;
    final String? message = response.statusMessage;
    if (statusCode == null || data == null) {
      return HttpResponseUnknownError();
    }
    if (statusCode >= 200 && statusCode <= 299) {
      return HttpResponseSuccess(data);
    }

    return HttpResponseError(statusCode, message);
  }

  HttpResponse _handleException(DioException exception) {
    if (exception.type == DioExceptionType.connectionError) {
      var baseUrl = _client.options.baseUrl;
      var isLocalhost =
          baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1");
      if (isLocalhost) {
        return HttpResponseConnectionError();
      }
      return HttpResponseUnknownError();
    } else {
      return HttpResponseUnknownError();
    }
  }
}

sealed class HttpResponse {}

class HttpResponseSuccess extends HttpResponse {
  final String body;

  HttpResponseSuccess(this.body);
}

class HttpResponseConnectionError extends HttpResponse {}

class HttpResponseUnknownError extends HttpResponse {}

class HttpResponseError extends HttpResponse {
  final int code;
  final String? message;

  HttpResponseError(this.code, this.message);
}
