import 'package:dio/dio.dart';

class HttpClient {
  final Dio _client;

  HttpClient(this._client);

  Future<HttpResponse> post(String endpoint, {required Object? data}) async {
    Response<String> response;
    try {
      response = await _client.post(endpoint, data: data);
    } on DioException catch (e) {
      return _handleException(e);
    }
    return _handleResponse(response);
  }

  Future<HttpResponse> get(String endpoint) async {
    Response<String> response;
    try {
      response = await _client.get(endpoint);
    } on DioException catch (e) {
      return _handleException(e);
    }
    return _handleResponse(response);
  }

  HttpResponse _handleResponse(Response<String> response) {
    final int? statusCode = response.statusCode;
    final String? data = response.data;
    if (statusCode == null || data == null) {
      return HttpResponseError();
    }
    if (statusCode < 200 && statusCode > 299) {
      return HttpResponseError();
    }
    return HttpResponseSuccess(data);
  }

  HttpResponse _handleException(DioException exception) {
    if (exception.type == DioExceptionType.connectionError) {
      var baseUrl = _client.options.baseUrl;
      var isLocalhost =
          baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1");
      if (isLocalhost) {
        return HttpResponseOllamaNotFound();
      }
      return HttpResponseError();
    } else {
      return HttpResponseError();
    }
  }
}

sealed class HttpResponse {}

class HttpResponseSuccess extends HttpResponse {
  final String body;

  HttpResponseSuccess(this.body);
}

class HttpResponseOllamaNotFound extends HttpResponse {}

class HttpResponseError extends HttpResponse {}
