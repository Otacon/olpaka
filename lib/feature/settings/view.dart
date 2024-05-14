import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/core/state/theme/theme_domain.dart';
import 'package:olpaka/feature/settings/view_model.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<SettingsViewModel>.reactive(
      viewModelBuilder: () => GetIt.I.get(),
      onViewModelReady: (viewModel) {
        viewModel.onCreate();
      },
      builder: (context, viewModel, child) {
        final state = viewModel.state;
        return Scaffold(
          appBar: _appBar(context),
          body: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16.0,
                  vertical: 8.0,
                ),
                child: Text(
                  S.current.settings_theme_section,
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
              ),
              _themeMode(
                context: context,
                themeMode: state.themeMode,
                onThemeModeChanged: viewModel.onThemeModeChanged,
              ),
              _themeColor(
                context: context,
                selectedColor: state.themeColor,
                onThemeColorChanged: viewModel.onSeedColorChanged,
              ),
              const SizedBox(height: 8.0),
              const Divider(height: 1),
              const Spacer(),
              const Divider(height: 1),
              ListTile(
                leading: const Icon(Icons.question_mark_outlined),
                title: Text(state.appVersion),
                subtitle: Text(S.current.settings_about_subtitle),
                onTap: () => showAboutDialog(
                    context: context,
                    applicationVersion: state.appVersion,
                    applicationLegalese: S.current.settings_about_author),
              )
            ],
          ),
        );
      },
    );
  }

  _appBar(BuildContext context) {
    return AppBar(
      elevation: 4,
      shadowColor: Theme.of(context).shadowColor,
      centerTitle: true,
      title: Text(
        S.current.settings_title,
        style: Theme.of(context).textTheme.headlineMedium,
      ),
    );
  }

  _themeMode({
    required BuildContext context,
    required OlpakaThemeMode themeMode,
    required Function(OlpakaThemeMode) onThemeModeChanged,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(
        vertical: 8.0,
        horizontal: 16.0,
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(S.current.settings_theme_mode),
          SegmentedButton<OlpakaThemeMode>(
              segments: <ButtonSegment<OlpakaThemeMode>>[
                ButtonSegment<OlpakaThemeMode>(
                  value: OlpakaThemeMode.system,
                  label: Text(S.current.settings_theme_mode_system),
                  icon: const Icon(Icons.brightness_auto),
                ),
                ButtonSegment<OlpakaThemeMode>(
                  value: OlpakaThemeMode.dark,
                  label: Text(S.current.settings_theme_mode_dark),
                  icon: const Icon(Icons.dark_mode),
                ),
                ButtonSegment<OlpakaThemeMode>(
                  value: OlpakaThemeMode.light,
                  label: Text(S.current.settings_theme_mode_light),
                  icon: const Icon(Icons.light_mode),
                )
              ],
              selected: <OlpakaThemeMode>{
                themeMode
              },
              onSelectionChanged: (selection) =>
                  onThemeModeChanged(selection.first))
        ],
      ),
    );
  }

  _themeColor({
    required BuildContext context,
    required OlpakaThemeColor selectedColor,
    required Function(OlpakaThemeColor) onThemeColorChanged,
  }) {
    final colorItems = OlpakaThemeColor.values
        .map((e) => _colorItem(
              context: context,
              color: e,
              isSelected: selectedColor == e,
              onThemeColorChanged: onThemeColorChanged,
            ))
        .toList();
    final children = [
          Text(S.current.settings_theme_color),
          const Spacer(),
        ] +
        colorItems;
    return Padding(
      padding: const EdgeInsets.symmetric(
        horizontal: 16.0,
        vertical: 8.0,
      ),
      child: Row(children: children),
    );
  }

  StatelessWidget _colorItem({
    required BuildContext context,
    required OlpakaThemeColor color,
    required bool isSelected,
    required Function(OlpakaThemeColor) onThemeColorChanged,
  }) {
    final Color uiColor;
    switch (color) {
      case OlpakaThemeColor.olpaka:
        uiColor = const Color(0xFFF21368);
      case OlpakaThemeColor.blue:
        uiColor = Colors.blue;
      case OlpakaThemeColor.purple:
        uiColor = Colors.purple;
      case OlpakaThemeColor.grey:
        uiColor = Colors.grey;
      case OlpakaThemeColor.red:
        uiColor = Colors.red;
      case OlpakaThemeColor.green:
        uiColor = Colors.green;
      case OlpakaThemeColor.orange:
        uiColor = Colors.orange;
    }
    return GestureDetector(
      onTap: () => {onThemeColorChanged(color)},
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4.0),
        child: Container(
          width: 32.0,
          height: 32.0,
          decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(
                color: Theme.of(context).colorScheme.onSurface,
                width: 1.0,
              ),
              color: uiColor),
          child: Visibility(
            visible: isSelected,
            child: const Icon(Icons.done),
          ),
        ),
      ),
    );
  }
}
