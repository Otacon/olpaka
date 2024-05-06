import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/chat/view.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:olpaka/home/view_model.dart';
import 'package:olpaka/models/view.dart';
import 'package:stacked/stacked.dart';

class HomeView extends StatelessWidget {
  final HomeTab tab;

  const HomeView({super.key, this.tab = HomeTab.chat});

  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<HomeViewModel>.reactive(
      viewModelBuilder: () => GetIt.I.get(),
      onViewModelReady: (viewModel) {
        viewModel.events.listen((event) {
          switch (event) {}
        });
        viewModel.onCreate(tab);
      },
      builder: (context, viewModel, child) {
        final content = switch(viewModel.selectedItem){
          0 => const ChatScreen(),
          1 => ModelsScreen(),
          int() => throw UnimplementedError(),
        };
        return Row(
            children: [
              NavigationRail(
                groupAlignment: 0.0,
                selectedIndex: viewModel.selectedItem,
                labelType: NavigationRailLabelType.all,
                onDestinationSelected: (index) => viewModel.onItemTapped(index),
                destinations: <NavigationRailDestination>[
                  NavigationRailDestination(
                    icon: const Icon(Icons.chat),
                    selectedIcon: const Icon(Icons.chat_outlined),
                    label: Text(S.current.home_tab_name_chat),
                  ),
                  NavigationRailDestination(
                    icon: const Icon(Icons.auto_awesome_outlined),
                    selectedIcon: const Icon(Icons.auto_awesome),
                    label: Text(S.current.home_tab_name_models),
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
}