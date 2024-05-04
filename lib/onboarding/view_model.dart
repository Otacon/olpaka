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
    }
    notifyListeners();
  }

  onCompleteInstallOllamaClicked() async {
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
        state = OnboardingStateSetupCors();
    }
    notifyListeners();
  }

  onBackInstallCorsClicked() async {
    state = OnboardingStateInstallOllama();
    notifyListeners();
  }

  onCompleteSetupCorsClicked() async {
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
        state = OnboardingStateSetupCors();
    }
    notifyListeners();
  }

  onCompleteInstallModelClicked() async {
    final result = await _repository.listModels();
    switch (result) {
      case ListModelsResultSuccess():
        if (result.models.isNotEmpty) {
          _events.add(OnboardingEventNavigateToChat());
        }
      case ListModelResultError():
      case ListModelResultConnectionError():
    }
  }

}

sealed class OnboardingEvent {}

class OnboardingEventNavigateToChat extends OnboardingEvent {}

sealed class OnboardingState {}

class OnboardingStateLoading extends OnboardingState {}

class OnboardingStateInstallOllama extends OnboardingState {}

class OnboardingStateSetupCors extends OnboardingState {
  final String? error;

  OnboardingStateSetupCors({this.error});
}

class OnboardingStateInstallModel extends OnboardingState {
  final String? error;

  OnboardingStateInstallModel({this.error});
}
