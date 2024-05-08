import 'package:get_it/get_it.dart';
import 'package:olpaka/settings/view_model.dart';

registerSettings() {
  final l = GetIt.instance;
  l.registerFactory(() => SettingsViewModel(l.get()));
}
