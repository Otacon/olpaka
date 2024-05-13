import 'package:get_it/get_it.dart';
import 'package:olpaka/app/view_model.dart';

registerApp() {
  final l = GetIt.instance;
  l.registerFactory(() => AppViewModel(l.get()));
}
