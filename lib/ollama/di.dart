import 'package:dio/dio.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/app/http_client.dart';
import 'package:olpaka/ollama/repository.dart';
import 'package:pretty_dio_logger/pretty_dio_logger.dart';

registerOllama() {
  final l = GetIt.instance;

  l.registerFactory(() {
    final client = Dio();
    client.options.baseUrl = "http://localhost:11434/api";
    client.interceptors.add(
      PrettyDioLogger(
          requestHeader: true,
          requestBody: true,
          responseBody: true,
          responseHeader: false,
          error: true,
          compact: true,
          maxWidth: 90),
    );
    return client;
  });
  l.registerFactory(() => HttpClient(l.get()));
  l.registerFactory(() => OllamaRepository(l.get()));
}
