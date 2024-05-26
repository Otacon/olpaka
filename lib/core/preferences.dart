import 'package:olpaka/core/state/theme/theme_domain.dart';
import 'package:shared_preferences/shared_preferences.dart';

abstract class Preferences {
  abstract final OlpakaThemeMode themeMode;
  abstract final OlpakaThemeColor themeColor;
  abstract final bool isGettingStartedViewed;

  Future<void> setThemeMode(OlpakaThemeMode mode);

  Future<void> setThemeColor(OlpakaThemeColor color);

  Future<void> setGettingStartedViewed();
}

class PreferenceDefault extends Preferences {
  static const String _keyThemeColor = "themeColor";
  static const String _keyThemeMode = "themeMode";
  static const String _keyGettingStartedViewed = "gettingStartedViewed";

  final SharedPreferences _sharedPreferences;

  PreferenceDefault(this._sharedPreferences);

  @override
  OlpakaThemeColor get themeColor {
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
  OlpakaThemeMode get themeMode {
    final colorString = _sharedPreferences.getString(_keyThemeMode);
    return switch (colorString) {
      "system" => OlpakaThemeMode.system,
      "dark" => OlpakaThemeMode.dark,
      "light" => OlpakaThemeMode.light,
      _ => OlpakaThemeMode.system
    };
  }

  @override
  bool get isGettingStartedViewed =>
      _sharedPreferences.getBool(_keyGettingStartedViewed) ?? false;

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

  @override
  Future<void> setGettingStartedViewed() async {
    await _sharedPreferences.setBool(_keyGettingStartedViewed, true);
  }
}
