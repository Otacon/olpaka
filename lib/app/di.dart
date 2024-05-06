

import 'package:get_it/get_it.dart';
import 'package:olpaka/chat/di.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:olpaka/home/di.dart';
import 'package:olpaka/models/di.dart';
import 'package:olpaka/ollama/di.dart';
import 'package:olpaka/onboarding/di.dart';

void registerModules() {
  final l = GetIt.instance;
  l.registerFactory(() => S.current);
  registerOllama();
  registerOnboarding();
  registerHome();
  registerChat();
  registerModels();
}