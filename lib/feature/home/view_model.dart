import 'dart:async';

import 'package:olpaka/core/state/model_state_holder.dart';
import 'package:stacked/stacked.dart';

class HomeViewModel extends BaseViewModel {

  final _events = StreamController<HomeEvent>.broadcast();
  final ModelStateHolder _modelStateHolder;

  HomeState state = HomeState(
    HomeTabChat(true),
    HomeTabDownloads(false, DownloadsState.none),
    HomeTabSettings(false),
  );

  Stream<HomeEvent> get events => _events.stream.map((val) {
        return val;
      });

  DownloadsState _downloadsState = DownloadsState.none;

  HomeViewModel(this._modelStateHolder);

  onCreate() async {
    _modelStateHolder.addListener(_onModelsChanged);
  }

  onItemTapped(int index) async {
    if(index == 1 && _downloadsState == DownloadsState.completed){
      _downloadsState = DownloadsState.none;
    }
    state = switch (index) {
      0 => HomeState(
          HomeTabChat(true),
          HomeTabDownloads(false, _downloadsState),
          HomeTabSettings(false),
        ),
      1 => HomeState(
          HomeTabChat(false),
          HomeTabDownloads(true, _downloadsState),
          HomeTabSettings(false),
        ),
      2 => HomeState(
          HomeTabChat(false),
          HomeTabDownloads(false, _downloadsState),
          HomeTabSettings(true),
        ),
      int() => throw UnimplementedError(),
    };
    notifyListeners();
  }

  _onModelsChanged() {
    var downloadState = DownloadsState.none;
    if(!state.downloads.isSelected){
      if(_modelStateHolder.downloadingModels.isNotEmpty){
        downloadState = DownloadsState.downloading;
      } else if(_downloadsState != DownloadsState.none){
        downloadState = DownloadsState.completed;
      }
    }
    _downloadsState = downloadState;
    state = HomeState(
        state.chat,
        HomeTabDownloads(state.downloads.isSelected, downloadState),
        state.settings
    );
    notifyListeners();

  }

  @override
  void dispose() {
    super.dispose();
    _modelStateHolder.removeListener(_onModelsChanged);
  }


}

class HomeState {
  final HomeTabChat chat;
  final HomeTabDownloads downloads;
  final HomeTabSettings settings;

  HomeState(this.chat, this.downloads, this.settings);
}

sealed class HomeEvent {}

sealed class HomeTab {
  final bool isSelected;

  HomeTab(this.isSelected);
}

class HomeTabChat extends HomeTab {
  HomeTabChat(super.isSelected);
}

class HomeTabDownloads extends HomeTab {
  final DownloadsState state;

  HomeTabDownloads(super.isSelected, this.state);
}

class HomeTabSettings extends HomeTab {
  HomeTabSettings(super.isSelected);
}

enum DownloadsState { none, downloading, completed }
