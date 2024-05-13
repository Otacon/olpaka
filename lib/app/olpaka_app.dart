import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/app/view_model.dart';
import 'package:olpaka/core/router.dart';
import 'package:olpaka/core/state/theme/theme_domain.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class OlpakaApp extends StatelessWidget {
  const OlpakaApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<AppViewModel>.reactive(
      viewModelBuilder: () => GetIt.I.get(),
      onViewModelReady: (viewModel){
        viewModel.onCreate();
      },
      builder: (context, viewModel, child) {
        final state = viewModel.state;
        final themeMode = switch(state.themeMode){
          OlpakaThemeMode.system => ThemeMode.system,
          OlpakaThemeMode.dark => ThemeMode.dark,
          OlpakaThemeMode.light => ThemeMode.light,
        };
        final color = switch(state.themeColor){
          OlpakaThemeColor.olpaka => const Color(0xFFF21368),
          OlpakaThemeColor.red => Colors.red,
          OlpakaThemeColor.purple => Colors.purple,
          OlpakaThemeColor.blue => Colors.blue,
          OlpakaThemeColor.orange => Colors.orange,
          OlpakaThemeColor.green => Colors.green,
          OlpakaThemeColor.grey => Colors.grey,
        };
        return MaterialApp.router(
          title: S.current.app_name,
          themeMode: themeMode,
          theme: ThemeData(
            colorScheme: ColorScheme.fromSeed(
              seedColor: color,
              brightness: Brightness.light,
            ),
            useMaterial3: true,
          ),
          darkTheme: ThemeData(
            colorScheme: ColorScheme.fromSeed(
              seedColor: color,
              brightness: Brightness.dark,
            ),
            useMaterial3: true,
          ),
          routerConfig: router,
        );
      },
    );
  }
}
