import 'package:get_it/get_it.dart';
import 'package:olpaka/chat/view_model.dart';

registerChat() {
  final l = GetIt.instance;
  l.registerFactory(() => ChatViewModel(l.get(), l.get()));
}