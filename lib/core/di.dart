import 'package:dio/dio.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/core/http_client.dart';
import 'package:olpaka/core/state/model_state_holder.dart';
import 'package:olpaka/core/state/theme_manager.dart';
import 'package:olpaka/feature/chat/di.dart';
import 'package:olpaka/feature/home/di.dart';
import 'package:olpaka/feature/models/di.dart';
import 'package:olpaka/feature/onboarding/di.dart';
import 'package:olpaka/feature/settings/di.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:olpaka/core/preferences.dart';
import 'package:pretty_dio_logger/pretty_dio_logger.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'ollama/repository.dart';

void registerModules() {
  final l = GetIt.instance;
  l.registerSingletonAsync<Preferences>(
      () async => PreferenceDefault(await SharedPreferences.getInstance()));
  l.registerLazySingleton(() => ThemeStateHolder(l.get()));
  l.registerFactory(() => S.current);

  l.registerFactory(() {
    final client = Dio();
    client.options.baseUrl = "http://localhost:11434/api";
    client.interceptors.add(
      PrettyDioLogger(
          requestHeader: true,
          requestBody: true,
          responseBody: true,
          responseHeader: true,
          error: true,
          compact: true,
          maxWidth: 90),
    );
    return client;
  });
  l.registerFactory(() => HttpClient(l.get()));
  l.registerFactory(() => OllamaRepository(l.get()));
  l.registerLazySingleton(() => ModelStateHolder(l.get()));
  registerOnboarding();
  registerHome();
  registerChat();
  registerModels();
  registerSettings();
}
