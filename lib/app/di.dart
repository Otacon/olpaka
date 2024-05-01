

import 'package:olpaka/chat/di.dart';
import 'package:olpaka/ollama/di.dart';
import 'package:olpaka/onboarding/di.dart';

void registerModules() {
  registerOllama();
  registerOnboarding();
  registerChat();
}