import 'package:olpaka/core/state/theme_manager.dart';
import 'package:stacked/stacked.dart';

class SettingsViewModel extends BaseViewModel {

  SettingsState state = SettingsState();
  final ThemeManager themeManager;

  SettingsViewModel(this.themeManager);

  onCreate() async {
    state = SettingsState(themeManager.themeMode, themeManager.themeColor);
  }

  onThemeModeChanged(OlpakaThemeMode themeMode) async {
    themeManager.setThemeMode(themeMode);
    state = SettingsState(themeMode, state.themeColor);
    notifyListeners();
  }

  onSeedColorChanged(OlpakaThemeColor color) async {
    themeManager.setThemeColor(color);
    state = SettingsState(state.themeMode, color);
    notifyListeners();
  }
}

class SettingsState {
  final OlpakaThemeMode themeMode;
  final OlpakaThemeColor themeColor;

  SettingsState([
    this.themeMode = OlpakaThemeMode.system,
    this.themeColor = OlpakaThemeColor.olpaka,
  ]);
}
