import 'dart:async';

import 'package:stacked/stacked.dart';

class HomeViewModel extends BaseViewModel {

  final _events = StreamController<HomeEvent>.broadcast();

  Stream<HomeEvent> get events =>
      _events.stream.map((val) {
        return val;
      });

  HomeViewModel();

  int selectedItem = 0;

  onCreate(HomeTab tab) async {
    selectedItem = switch(tab){
      HomeTab.chat => 0,
      HomeTab.models => 1,
      HomeTab.settings => 2,
    };
    notifyListeners();
  }

  onItemTapped(int index) async {
    selectedItem = index;
    notifyListeners();
  }
}

sealed class HomeEvent {}


enum HomeTab {
  chat,
  models,
  settings
}