import 'package:fetch_client/fetch_client.dart';
import 'package:flutter/foundation.dart';
import 'package:get_it/get_it.dart';
import 'package:http/http.dart';
import 'package:olpaka/app/di.dart';
import 'package:olpaka/core/http_client/http_client.dart';
import 'package:olpaka/core/http_client/url_provider.dart';
import 'package:olpaka/core/state/di.dart';
import 'package:olpaka/feature/chat/di.dart';
import 'package:olpaka/feature/home/di.dart';
import 'package:olpaka/feature/models/di.dart';
import 'package:olpaka/feature/onboarding/di.dart';
import 'package:olpaka/feature/settings/di.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:olpaka/core/preferences.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'ollama/repository.dart';

void registerModules() {
  final l = GetIt.instance;
  l.registerSingletonAsync<Preferences>(
      () async => PreferenceDefault(await SharedPreferences.getInstance()));

  l.registerFactory(() => S.current);

  registerStateHolders();

  l.registerLazySingleton(() => UrlProvider());
  l.registerFactory(() {
    if(kIsWeb){
      return FetchClient(mode: RequestMode.cors);
    } else {
      return Client();
    }

  });
  l.registerFactory(() => HttpClient(l.get(), l.get()));
  l.registerFactory(() => OllamaRepository(l.get()));
  registerApp();
  registerOnboarding();
  registerHome();
  registerChat();
  registerModels();
  registerSettings();
}
