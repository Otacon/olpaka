import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:go_router/go_router.dart';
import 'package:olpaka/onboarding/view_model.dart';
import 'package:stacked/stacked.dart';
import 'package:url_launcher/url_launcher_string.dart';

class OnboardingScreen extends StatelessWidget {
  const OnboardingScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<OnboardingViewModel>.reactive(
      viewModelBuilder: () => GetIt.I.get(),
      onViewModelReady: (viewModel) {
        viewModel.events.listen((event) {
          switch (event) {
            case OnboardingEventNavigateToChat():
              context.go("/chat");
          }
        });
        viewModel.onCreate();
      },
      builder: (context, viewModel, child) {
        final state = viewModel.state;
        return Scaffold(
          appBar: AppBar(
            elevation: 4,
            shadowColor: Theme.of(context).shadowColor,
            centerTitle: true,
            title: Text(
              "Olpaka",
              style: Theme.of(context).textTheme.headlineMedium,
            ),
          ),
          body: switch (state) {
            OnboardingStateLoading() => _Loading(),
            OnboardingStateInstallOllama() => _Loaded(
                step: _Step.installOllama,
                onNextClicked: viewModel.onDoneClicked,
              ),
            OnboardingStateSetupCors() => _Loaded(
                step: _Step.setupCors,
                onNextClicked: viewModel.onDoneClicked,
              ),
            OnboardingStateInstallModel() => _Loaded(
                step: _Step.installModel,
                onNextClicked: viewModel.onDoneClicked,
              ),
          },
        );
      },
    );
  }
}

class _Loading extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return const Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          CircularProgressIndicator(),
          SizedBox(height: 16),
          Text("Loading..."),
        ],
      ),
    );
  }
}

class _Loaded extends StatelessWidget {
  final _Step step;
  final Function() onNextClicked;

  const _Loaded({required this.step, required this.onNextClicked});

  @override
  Widget build(BuildContext context) {
    final int currentStep;
    final StepState installOllamaState;
    final StepState setupCorsState;
    final StepState installModelState;
    switch (step) {
      case _Step.installOllama:
        currentStep = 0;
        installOllamaState = StepState.indexed;
        setupCorsState = StepState.indexed;
        installModelState = StepState.indexed;
      case _Step.setupCors:
        currentStep = 1;
        installOllamaState = StepState.complete;
        setupCorsState = StepState.complete;
        installModelState = StepState.indexed;
      case _Step.installModel:
        currentStep = 2;
        installOllamaState = StepState.complete;
        setupCorsState = StepState.complete;
        installModelState = StepState.editing;
    }

    return Stepper(
      type: StepperType.horizontal,
      currentStep: currentStep,
      onStepContinue: onNextClicked,
      steps: [
        Step(
          title: const Text("Install Ollama"),
          content: _StepInstallOllama(),
          state: installOllamaState,
          isActive: currentStep >= 0,
        ),
        Step(
          title: const Text("Setup CORS"),
          content: const Text("Cors!"),
          state: setupCorsState,
          isActive: currentStep >= 1,
        ),
        Step(
          title: const Text("Install Model"),
          content: const Text("Model!"),
          state: installModelState,
          isActive: currentStep >= 2,
        ),
      ],
    );
  }
}

class _StepInstallOllama extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text("In order to get started, you should install Ollama."),
          const SizedBox(height: 16),
          const Text("You can get Ollama from the official website. "),
          const SizedBox(height: 16),
          ElevatedButton(
              onPressed: () => launchUrlString("https://ollama.com/download"),
              child: const Text("Install Ollama")),
          const SizedBox(height: 16),
          const Text(
              "Once you've installed the software, make sure the service is running and click on continue"),
        ],
      ),
    );
  }
}

enum _Step {
  installOllama,
  setupCors,
  installModel,
}
