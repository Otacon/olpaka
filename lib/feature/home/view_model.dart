import 'dart:async';

import 'package:olpaka/core/state/models/model_state_holder.dart';
import 'package:olpaka/feature/home/events.dart';
import 'package:olpaka/feature/home/state.dart';
import 'package:stacked/stacked.dart';

class HomeViewModel extends BaseViewModel {

  final _events = StreamController<HomeEvent>.broadcast();
  final ModelStateHolder _modelStateHolder;

  HomeState state = HomeState(
    HomeTabChat(true),
    HomeTabDownloads(false, DownloadsState.none),
    HomeTabSettings(false),
  );

  Stream<HomeEvent> get events => _events.stream.map((val) => val);

  DownloadsState _downloadsState = DownloadsState.none;

  HomeViewModel(this._modelStateHolder);

  onCreate() async {
    _modelStateHolder.cachedModels.addListener(_onModelsChanged);
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
      if(_modelStateHolder.downloadingModels.value.isNotEmpty){
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
    _modelStateHolder.cachedModels.removeListener(_onModelsChanged);
    super.dispose();
  }

}

