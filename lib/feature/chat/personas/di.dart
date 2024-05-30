import 'package:get_it/get_it.dart';
import 'package:olpaka/feature/chat/personas/view_model.dart';

registerPersonas() {
  final l = GetIt.instance;

  l.registerFactory(() => PersonasViewModel(l.get(), l.get()));
}
