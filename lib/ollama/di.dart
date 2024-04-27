import 'package:dio/dio.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/ollama/repository.dart';

registerOllama() {
  final l = GetIt.instance;
  final client = Dio();
  l.registerFactory(() => OllamaRepository(client));
}