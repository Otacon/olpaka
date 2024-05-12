import 'package:olpaka/core/state/theme/theme_domain.dart';

class SettingsState {
  final OlpakaThemeMode themeMode;
  final OlpakaThemeColor themeColor;

  SettingsState([
    this.themeMode = OlpakaThemeMode.system,
    this.themeColor = OlpakaThemeColor.olpaka,
  ]);
}