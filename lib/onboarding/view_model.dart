import 'dart:async';

import 'package:olpaka/generated/l10n.dart';
import 'package:olpaka/ollama/repository.dart';
import 'package:stacked/stacked.dart';

class OnboardingViewModel extends BaseViewModel {
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
          _events.add(OnboardingEventNavigateToHome());
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
          _events.add(OnboardingEventNavigateToHome());
        }
      case ListModelResultError():
      case ListModelResultConnectionError():
        state = OnboardingStateSetupCors();
    }
    notifyListeners();
  }

  onCompleteSetupCorsClicked() async {
    final result = await _repository.listModels();
    switch (result) {
      case ListModelsResultSuccess():
        if (result.models.isEmpty) {
          state = OnboardingStateInstallModel();
        } else {
          _events.add(OnboardingEventNavigateToHome());
        }
      case ListModelResultError():
      case ListModelResultConnectionError():
        state = OnboardingStateSetupCors(error: S.current.onboarding_configure_cors_error);
    }
    notifyListeners();
  }

  onCompleteInstallModelClicked() async {
    final result = await _repository.listModels();
    switch (result) {
      case ListModelsResultSuccess():
        if (result.models.isEmpty) {
          state = OnboardingStateInstallModel(error: S.current.onboarding_install_model_error);
        } else {
          _events.add(OnboardingEventNavigateToHome());
        }
      case ListModelResultError():
      case ListModelResultConnectionError():
    }
    notifyListeners();
  }

}

sealed class OnboardingEvent {}

class OnboardingEventNavigateToHome extends OnboardingEvent {}

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
