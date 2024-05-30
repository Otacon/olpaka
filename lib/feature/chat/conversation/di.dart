import 'package:get_it/get_it.dart';
import 'package:olpaka/feature/chat/conversation/view_model.dart';

registerConversation() {
  final l = GetIt.instance;
  l.registerFactory(() => ConversationViewModel(l.get(), l.get(), l.get(), l.get()));
}
