import 'package:dio/dio.dart';

import 'logger.dart';
import 'package:http/http.dart' as http;

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

  //DIO
  // Future<HttpResponse> get(String endpoint) async {
  //   Response<String> response;
  //   try {
  //     print("Performing GET @ $endpoint...");
  //     response = await _client.get(endpoint);
  //   } on DioException catch (e) {
  //     return _handleException(e);
  //   }
  //   return _handleResponse(response);
  // }

  //DIO
  Future<HttpResponse> get(String endpoint) async {
    var url = Uri.http('localhost:11434', 'api/tags');
    try{
      await http.get(url);
      return HttpResponseUnknownError();
    } on http.ClientException catch(e){
      print("Whooopisee daisy ${e.message}");
      return HttpResponseUnknownError();
    } catch(exception){
      print("Whoopsie! $exception");
      print("Exception connectionError");
      return HttpResponseUnknownError();
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
    } else if(statusCode == 403){
      return HttpResponseCorsError();
    }

    return HttpResponseError(statusCode, message);
  }

  HttpResponse _handleException(DioException exception) {
    if (exception.type == DioExceptionType.connectionError) {
      print("Exception connectionError");
      print("Message ${exception.message}");
      print("StatusCode ${exception.response?.statusCode}");
      print("StatusMessage ${exception.response?.statusMessage}");
      print("Error ${exception.error}");
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

class HttpResponseCorsError extends HttpResponse {}

class HttpResponseUnknownError extends HttpResponse {}

class HttpResponseError extends HttpResponse {
  final int code;
  final String? message;

  HttpResponseError(this.code, this.message);

}

