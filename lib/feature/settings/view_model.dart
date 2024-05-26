import 'dart:async';

import 'package:olpaka/core/analytics/analytics.dart';
import 'package:olpaka/core/analytics/event.dart';
import 'package:olpaka/core/analytics/screen_view.dart';
import 'package:olpaka/core/state/theme/theme_domain.dart';
import 'package:olpaka/core/state/theme/theme_state_holder.dart';
import 'package:olpaka/feature/settings/events.dart';
import 'package:olpaka/feature/settings/state.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:stacked/stacked.dart';

class SettingsViewModel extends BaseViewModel {
  final ThemeStateHolder _themeManager;
  final Analytics _analytics;

  final _events = StreamController<SettingsEvent>.broadcast();
  Stream<SettingsEvent> get events => _events.stream.map((val) => val);

  SettingsState state = SettingsState();

  SettingsViewModel(this._themeManager, this._analytics);

  onCreate() async {
    _analytics.screenView(ScreenViewSettings());
    state = SettingsState(
      _themeManager.themeMode.value,
      _themeManager.themeColor.value,
      await _appVersion(),
    );
    notifyListeners();
  }

  onThemeModeChanged(OlpakaThemeMode themeMode) async {
    _analytics.event(EventChangeThemeMode(themeMode));
    _themeManager.setThemeMode(themeMode);
    state = SettingsState(themeMode, state.themeColor, state.appVersion);
    notifyListeners();
  }

  onSeedColorChanged(OlpakaThemeColor color) async {
    _analytics.event(EventChangeThemeColor(color));
    _themeManager.setThemeColor(color);
    state = SettingsState(state.themeMode, color, state.appVersion);
    notifyListeners();
  }

  Future<String> _appVersion() async {
    final packageInfo = await PackageInfo.fromPlatform();
    return "${packageInfo.appName} v${packageInfo.version}";
  }

  onShowOnboardingClicked() async {
    _analytics.event(EventLaunchGettingStarted());
    _events.add(OpenGettingStartedSettingsEvent());
  }

  onAboutClicked() async {
    _analytics.event(EventAboutPressed());
    _events.add(OpenAboutSettingsEvent());
  }
}
