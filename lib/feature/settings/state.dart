
import 'package:olpaka/core/state/theme_manager.dart';

class SettingsState {
  final OlpakaThemeMode themeMode;
  final OlpakaThemeColor themeColor;

  SettingsState([
    this.themeMode = OlpakaThemeMode.system,
    this.themeColor = OlpakaThemeColor.olpaka,
  ]);
}