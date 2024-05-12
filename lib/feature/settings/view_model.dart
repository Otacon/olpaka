import 'package:olpaka/core/state/theme_manager.dart';
import 'package:olpaka/feature/settings/state.dart';
import 'package:stacked/stacked.dart';

class SettingsViewModel extends BaseViewModel {

  SettingsState state = SettingsState();
  final ThemeStateHolder themeManager;

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

