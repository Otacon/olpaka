import 'package:olpaka/core/state/theme_manager.dart';
import 'package:shared_preferences/shared_preferences.dart';

abstract class Preferences {
  OlpakaThemeMode getThemeMode();

  Future<void> setThemeMode(OlpakaThemeMode mode);

  OlpakaThemeColor getThemeColor();

  Future<void> setThemeColor(OlpakaThemeColor color);
}

class PreferenceDefault extends Preferences {
  static const String _keyThemeColor = "themeColor";
  static const String _keyThemeMode = "themeMode";

  final SharedPreferences _sharedPreferences;

  PreferenceDefault(this._sharedPreferences);

  @override
  OlpakaThemeColor getThemeColor() {
    final colorString = _sharedPreferences.getString(_keyThemeColor);
    return switch (colorString) {
      "olpaka" => OlpakaThemeColor.olpaka,
      "blue" => OlpakaThemeColor.blue,
      "grey" => OlpakaThemeColor.grey,
      "orange" => OlpakaThemeColor.orange,
      "green" => OlpakaThemeColor.green,
      "red" => OlpakaThemeColor.red,
      "purple" => OlpakaThemeColor.purple,
      _ => OlpakaThemeColor.olpaka
    };
  }

  @override
  OlpakaThemeMode getThemeMode() {
    final colorString = _sharedPreferences.getString(_keyThemeMode);
    return switch (colorString) {
      "system" => OlpakaThemeMode.system,
      "dark" => OlpakaThemeMode.dark,
      "light" => OlpakaThemeMode.light,
      _ => OlpakaThemeMode.system
    };
  }

  @override
  Future<void> setThemeColor(OlpakaThemeColor color) async {
    final colorString = switch (color) {
      OlpakaThemeColor.olpaka => "olpaka",
      OlpakaThemeColor.blue => "blue",
      OlpakaThemeColor.green => "green",
      OlpakaThemeColor.orange => "orange",
      OlpakaThemeColor.red => "red",
      OlpakaThemeColor.purple => "purple",
      OlpakaThemeColor.grey => "grey",
    };
    await _sharedPreferences.setString(_keyThemeColor, colorString);
  }

  @override
  Future<void> setThemeMode(OlpakaThemeMode mode) async {
    final modeString = switch (mode) {
      OlpakaThemeMode.system => "system",
      OlpakaThemeMode.dark => "dark",
      OlpakaThemeMode.light => "light",
    };
    await _sharedPreferences.setString(_keyThemeMode, modeString);
  }
}
