import 'package:olpaka/core/state/theme/theme_domain.dart';

class SettingsState {
  final OlpakaThemeMode themeMode;
  final OlpakaThemeColor themeColor;
  final String appVersion;

  SettingsState([
    this.themeMode = OlpakaThemeMode.system,
    this.themeColor = OlpakaThemeColor.olpaka,
    this.appVersion = "",
  ]);
}
