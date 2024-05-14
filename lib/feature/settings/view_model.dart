import 'package:olpaka/core/state/theme/theme_domain.dart';
import 'package:olpaka/core/state/theme/theme_state_holder.dart';
import 'package:olpaka/feature/settings/state.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:stacked/stacked.dart';

class SettingsViewModel extends BaseViewModel {
  SettingsState state = SettingsState();
  final ThemeStateHolder _themeManager;

  SettingsViewModel(this._themeManager);

  onCreate() async {
    state = SettingsState(
      _themeManager.themeMode.value,
      _themeManager.themeColor.value,
      await _appVersion(),
    );
    notifyListeners();
  }

  onThemeModeChanged(OlpakaThemeMode themeMode) async {
    _themeManager.setThemeMode(themeMode);
    state = SettingsState(themeMode, state.themeColor, state.appVersion);
    notifyListeners();
  }

  onSeedColorChanged(OlpakaThemeColor color) async {
    _themeManager.setThemeColor(color);
    state = SettingsState(state.themeMode, color, state.appVersion);
    notifyListeners();
  }

  Future<String> _appVersion() async {
    final packageInfo = await PackageInfo.fromPlatform();
    return "${packageInfo.appName} v${packageInfo.version}";
  }
}
