import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/feature/chat/view.dart';
import 'package:olpaka/feature/home/view_model.dart';
import 'package:olpaka/feature/models/view.dart';
import 'package:olpaka/feature/settings/view.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class HomeView extends StatelessWidget {
  const HomeView({super.key});

  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<HomeViewModel>.reactive(
      viewModelBuilder: () => GetIt.I.get(),
      onViewModelReady: (viewModel) {
        viewModel.events.listen((event) {
          switch (event) {}
        });
        viewModel.onCreate();
      },
      builder: (context, viewModel, child) {
        final HomeState state = viewModel.state;
        final Widget content;
        final int selectedIndex;
        if (state.settings.isSelected) {
          content = const SettingsScreen();
          selectedIndex = 2;
        } else if (state.downloads.isSelected) {
          content = ModelsScreen();
          selectedIndex = 1;
        } else {
          content = const ChatScreen();
          selectedIndex = 0;
        }

        return Row(
          children: [
            NavigationRail(
              groupAlignment: 0.0,
              selectedIndex: selectedIndex,
              labelType: NavigationRailLabelType.all,
              onDestinationSelected: (index) => viewModel.onItemTapped(index),
              destinations: <NavigationRailDestination>[
                NavigationRailDestination(
                  icon: const Icon(Icons.chat),
                  selectedIcon: const Icon(Icons.chat_outlined),
                  label: Text(S.current.home_tab_name_chat),
                ),
                NavigationRailDestination(
                  icon: _downloadsIcon(context, false, state.downloads.state),
                  selectedIcon:
                      _downloadsIcon(context, true, state.downloads.state),
                  label: Text(S.current.home_tab_name_models),
                ),
                NavigationRailDestination(
                  icon: const Icon(Icons.settings_outlined),
                  selectedIcon: const Icon(Icons.settings),
                  label: Text(S.current.home_tab_name_settings),
                ),
              ],
            ),
            const VerticalDivider(thickness: 1, width: 1),
            Expanded(child: content)
          ],
        );
      },
    );
  }

  _downloadsIcon(BuildContext context, bool isSelected, DownloadsState state) {
    final Icon icon;
    if (isSelected) {
      icon = const Icon(Icons.smart_toy);
    } else {
      icon = const Icon(Icons.smart_toy_outlined);
    }

    return switch (state) {
      DownloadsState.none => icon,
      DownloadsState.downloading => Badge(
          backgroundColor: Colors.orange,
          alignment: Alignment.topLeft,
          child: icon,
        ),
      DownloadsState.completed => Badge(
          backgroundColor: Colors.green,
          alignment: Alignment.topLeft,
          child: icon,
        ),
    };
  }
}