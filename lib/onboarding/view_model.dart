import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:olpaka/ollama/repository.dart';

class OnboardingViewModel extends ChangeNotifier {
  final OllamaRepository _repository;

  OnboardingViewModel(this._repository);

  OnboardingState state = OnboardingStateLoading();

  final _events = StreamController<OnboardingEvent>.broadcast();

  Stream<OnboardingEvent> get events => _events.stream.map((val) => val);

  onCreate() async {
    await _refreshState();
  }

  onDoneClicked() async {
    await _refreshState();
  }

  _refreshState() async {
    final result = await _repository.listModels();
    switch (result) {
      case ListModelsResultSuccess():
        if (result.models.isEmpty) {
          state = OnboardingStateInstallModel();
        } else {
          _events.add(OnboardingEventNavigateToChat());
        }
      case ListModelResultError():
      case ListModelResultConnectionError():
        state = OnboardingStateInstallOllama();

      case ListModelResultCorsError():
        state = OnboardingStateSetupCors();
    }
    notifyListeners();
  }
}

sealed class OnboardingEvent {}

class OnboardingEventNavigateToChat extends OnboardingEvent {}

sealed class OnboardingState {}

class OnboardingStateLoading extends OnboardingState {}

class OnboardingStateInstallOllama extends OnboardingState {}

class OnboardingStateSetupCors extends OnboardingState {}

class OnboardingStateInstallModel extends OnboardingState {}
