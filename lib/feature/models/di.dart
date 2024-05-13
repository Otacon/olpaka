import 'package:get_it/get_it.dart';
import 'package:olpaka/feature/models/view_model.dart';

registerModels() {
  final l = GetIt.instance;
  l.registerFactory(() => ModelsViewModel(l.get()));
}
