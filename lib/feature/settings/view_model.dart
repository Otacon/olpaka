import 'package:olpaka/core/state/theme/theme_domain.dart';
import 'package:olpaka/core/state/theme/theme_state_holder.dart';
import 'package:olpaka/feature/settings/state.dart';
import 'package:stacked/stacked.dart';

class SettingsViewModel extends BaseViewModel {
  SettingsState state = SettingsState();
  final ThemeStateHolder _themeManager;

  SettingsViewModel(this._themeManager);

  onCreate() async {
    state = SettingsState(
      _themeManager.themeMode.value,
      _themeManager.themeColor.value,
    );
  }

  onThemeModeChanged(OlpakaThemeMode themeMode) async {
    _themeManager.setThemeMode(themeMode);
    state = SettingsState(themeMode, state.themeColor);
    notifyListeners();
  }

  onSeedColorChanged(OlpakaThemeColor color) async {
    _themeManager.setThemeColor(color);
    state = SettingsState(state.themeMode, color);
    notifyListeners();
  }

}
