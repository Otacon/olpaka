import 'package:get_it/get_it.dart';
import 'package:olpaka/feature/getting_started/view_model.dart';

registerGettingStarted() {
  final l = GetIt.instance;
  l.registerFactory(() => GettingStartedViewModel(l.get(), l.get(), l.get()));
}
