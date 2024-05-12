
import 'package:get_it/get_it.dart';
import 'package:olpaka/core/state/theme_manager.dart';

import 'chat/chat_state_holder.dart';
import 'models/model_state_holder.dart';

registerStateHolders() {
  final l = GetIt.instance;
  l.registerLazySingleton(() => ThemeStateHolder(l.get()));
  l.registerLazySingleton(() => ModelStateHolder(l.get()));
  l.registerLazySingleton(() => ChatStateHolder(l.get()));
}