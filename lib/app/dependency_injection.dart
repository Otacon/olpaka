

import 'package:olpaka/chat/di.dart';
import 'package:olpaka/ollama/di.dart';

void registerModules() {
  registerOllama();
  registerChat();
}