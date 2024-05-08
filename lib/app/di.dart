import 'package:get_it/get_it.dart';
import 'package:olpaka/chat/di.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:olpaka/home/di.dart';
import 'package:olpaka/models/di.dart';
import 'package:olpaka/ollama/di.dart';
import 'package:olpaka/onboarding/di.dart';
import 'package:olpaka/preferences/preferences.dart';
import 'package:olpaka/settings/ThemeManager.dart';
import 'package:olpaka/settings/di.dart';
import 'package:shared_preferences/shared_preferences.dart';

void registerModules() {
  final l = GetIt.instance;
  l.registerSingletonAsync<Preferences>(
      () async => PreferenceDefault(await SharedPreferences.getInstance()));
  l.registerLazySingleton(() => ThemeManager(l.get()));
  l.registerFactory(() => S.current);
  registerOllama();
  registerOnboarding();
  registerHome();
  registerChat();
  registerModels();
  registerSettings();
}
