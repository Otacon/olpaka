import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart';
import 'package:olpaka/core/http_client/url_provider.dart';
import 'package:olpaka/core/logger.dart';

class HttpClient {
  final Client _client;
  final UrlProvider _urlProvider;

  HttpClient(this._client, this._urlProvider);

  Future<HttpResponse> post(
    String endpoint, {
    required Map<String, Object>? data,
  }) async {
    try {
      final url = _urlProvider.forPath(endpoint);
      final body = json.encode(data);
      logger.d("> POST @ $url\n$body");
      final response = await _client.post(url, body: body);
      return _handleResponse(response);
    } on SocketException catch (e) {
      return _handleException(e);
    }
  }

  Stream<String> postStreaming(
    String endpoint, {
    required Map<String, Object>? data,
  }) async* {
    final url = _urlProvider.forPath(endpoint);
    final request = Request("POST", url);
    final body = json.encode(data);
    request.body = body;
    logger.d("> POST (stream) @ $url\n$body");
    final response = await _client.send(request);
    yield* response.stream
        .transform(const Utf8Decoder())
        .transform(const LineSplitter());
  }

  Future<HttpResponse> delete(
    String endpoint, {
    Map<String, Object>? data,
  }) async {
    try {
      final url = _urlProvider.forPath(endpoint);
      final body = json.encode(data);
      logger.d("> DELETE @ $url\n$body");
      final response = await _client.delete(url, body: body);
      return _handleResponse(response);
    } on SocketException catch (e) {
      return _handleException(e);
    }
  }

  Future<HttpResponse> get(String endpoint) async {
    try {
      final url = _urlProvider.forPath(endpoint);
      logger.d("> GET @ $url");
      final response = await _client.get(url);
      return _handleResponse(response);
    } on SocketException catch (e) {
      return _handleException(e);
    }
  }

  HttpResponse _handleResponse(Response response) {
    final statusCode = response.statusCode;
    final body = response.body;
    final method = response.request?.method;
    final url = response.request?.url;
    logger.d("< $method @ $url\n$statusCode\n$body");
    if (statusCode >= 200 && statusCode <= 299) {
      return HttpResponseSuccess(body);
    }

    return HttpResponseError(statusCode, response.body);
  }

  HttpResponse _handleException(SocketException exception) {
    return HttpResponseConnectionError();
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
