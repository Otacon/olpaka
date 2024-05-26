import 'dart:async';

import 'package:olpaka/core/analytics/analytics.dart';
import 'package:olpaka/core/analytics/event.dart';
import 'package:olpaka/core/analytics/screen_view.dart';
import 'package:olpaka/core/ollama/list_models_result.dart';
import 'package:olpaka/core/ollama/repository.dart';
import 'package:olpaka/core/preferences.dart';
import 'package:olpaka/feature/getting_started/events.dart';
import 'package:olpaka/feature/getting_started/state.dart';
import 'package:stacked/stacked.dart';

class GettingStartedViewModel extends BaseViewModel {
  final Analytics _analytics;
  final Preferences _preferences;
  final OllamaRepository _ollama;

  GettingStartedState state = GettingStartedState();
  final _events = StreamController<GettingStartedEvent>.broadcast();
  Stream<GettingStartedEvent> get events => _events.stream.map((val) => val);

  GettingStartedViewModel(this._analytics, this._preferences, this._ollama);

  onCreate() async {
    _analytics.screenView(ScreenViewGettingStarted());
  }

  onPreviousClicked() async {
    _analytics.event(EventGettingStartedPrevious(state.currentStep));
    _updateState(state.currentStep - 1);
    notifyListeners();
  }

  onNextClicked() async {
    _analytics.event(EventGettingStartedNext(state.currentStep));
    _updateState(state.currentStep + 1);
    notifyListeners();
  }

  onFinishClicked() async {
    _analytics.event(EventGettingStartedFinish());
    await _preferences.setGettingStartedViewed();
    _events.add(CloseGettingStartedEvent());
  }

  onCheckConnectionClicked() async {
    final response = await _ollama.listModels();
    final bool connected;
    switch(response){
      case ListModelsResultSuccess():
        connected = true;
      case ListModelResultConnectionError():
      case ListModelResultError():
        connected = false;
    }
    _analytics.event(EventCheckConnectionPressed(connected));
    state = GettingStartedState(
      state.currentStep,
      state.showPrevious,
      state.isLastStep,
      connected,
    );
    notifyListeners();
  }

  onDownloadOllamaClicked() async {
    _analytics.event(EventDownloadOllamaPressed());
    _events.add(OpenDownloadOllamaGettingStartedEvent());
  }

  onSetupCorsClicked() async {
    _analytics.event(EventCorsLinkPressed());
    _events.add(OpenSetupCorsGettingStartedEvent());
  }

  _updateState(int potentialNextStep) {
    int nextStep;
    bool showPrevious;
    bool isLastStep;

    if (potentialNextStep <= 0) {
      nextStep = 0;
      showPrevious = false;
      isLastStep = false;
    } else if (potentialNextStep > 0 && potentialNextStep < 2) {
      nextStep = potentialNextStep;
      showPrevious = true;
      isLastStep = false;
    } else {
      nextStep = 2;
      showPrevious = true;
      isLastStep = true;
    }

    state = GettingStartedState(
      nextStep,
      showPrevious,
      isLastStep,
      state.isConnected,
    );
  }

}
