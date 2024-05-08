import 'package:flutter/cupertino.dart';
import 'package:olpaka/core/preferences.dart';

class ThemeManager with ChangeNotifier {
  final Preferences _preferences;

  ThemeManager(this._preferences);

  OlpakaThemeMode get themeMode => _preferences.getThemeMode();

  setThemeMode(OlpakaThemeMode value)  {
    _preferences.setThemeMode(value);
    notifyListeners();
  }

  OlpakaThemeColor get themeColor => _preferences.getThemeColor();

  setThemeColor(OlpakaThemeColor value) {
    _preferences.setThemeColor(value);
    notifyListeners();
  }
}

enum OlpakaThemeMode { system, dark, light }

enum OlpakaThemeColor { olpaka, blue, green, orange, red, purple, grey }
