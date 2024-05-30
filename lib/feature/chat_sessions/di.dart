import 'package:get_it/get_it.dart';
import 'package:olpaka/feature/chat_sessions/view_model.dart';

registerChatSessions() {
  final l = GetIt.instance;

  l.registerFactory(() => ChatSessionsViewModel(l.get(), l.get()));
}
