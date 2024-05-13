import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:olpaka/core/preferences.dart';
import 'package:olpaka/core/state/theme/theme_domain.dart';

class ThemeStateHolder {
  final Preferences _preferences;

  final ValueNotifier<OlpakaThemeColor> themeColor;
  final ValueNotifier<OlpakaThemeMode> themeMode;

  ThemeStateHolder(Preferences preferences)
      : _preferences = preferences,
        themeColor = ValueNotifier<OlpakaThemeColor>(preferences.themeColor),
        themeMode = ValueNotifier<OlpakaThemeMode>(preferences.themeMode);

  setThemeMode(OlpakaThemeMode value) {
    _preferences.setThemeMode(value);
    themeMode.value = value;
  }

  setThemeColor(OlpakaThemeColor value) {
    _preferences.setThemeColor(value);
    themeColor.value = value;
  }
}
