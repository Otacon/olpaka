import 'package:olpaka/app/state.dart';
import 'package:olpaka/core/state/theme/theme_state_holder.dart';
import 'package:stacked/stacked.dart';

class AppViewModel extends BaseViewModel {
  final ThemeStateHolder _themeStateHolder;
  AppState state;

  AppViewModel(ThemeStateHolder themeStateHolder)
      : _themeStateHolder = themeStateHolder,
        state = AppState(
          themeStateHolder.themeMode.value,
          themeStateHolder.themeColor.value,
        );

  onCreate() async {
    _themeStateHolder.themeColor.addListener(_onColorChanged);
    _themeStateHolder.themeMode.addListener(_onThemeChanged);
  }

  _onColorChanged() {
    state = AppState(state.themeMode, _themeStateHolder.themeColor.value);
    notifyListeners();
  }

  _onThemeChanged() {
    state = AppState(_themeStateHolder.themeMode.value, state.themeColor);
    notifyListeners();
  }

  @override
  void dispose() {
    _themeStateHolder.themeColor.removeListener(_onColorChanged);
    _themeStateHolder.themeMode.removeListener(_onThemeChanged);
    super.dispose();
  }
}
