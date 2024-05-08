import 'package:flutter/cupertino.dart';
import 'package:olpaka/preferences/preferences.dart';
import 'package:olpaka/settings/view_model.dart';

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
