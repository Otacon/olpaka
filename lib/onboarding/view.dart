import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:go_router/go_router.dart';
import 'package:markdown_widget/markdown_widget.dart';
import 'package:olpaka/generated/l10n.dart';
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
              S.current.onboarding_title,
              style: Theme.of(context).textTheme.headlineMedium,
            ),
          ),
          body: switch (state) {
            OnboardingStateLoading() => _Loading(),
            OnboardingStateInstallOllama() => _Loaded(
                step: _Step.installOllama,
                error: null,
                onNextClicked: viewModel.onCompleteInstallOllamaClicked,
              ),
            OnboardingStateSetupCors() => _Loaded(
                step: _Step.setupCors,
                error: state.error,
                onNextClicked: viewModel.onCompleteSetupCorsClicked,
              ),
            OnboardingStateInstallModel() => _Loaded(
                step: _Step.installModel,
                error: state.error,
                onNextClicked: viewModel.onCompleteInstallModelClicked,
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
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const CircularProgressIndicator(),
          const SizedBox(height: 16),
          Text(S.current.onboarding_loading),
        ],
      ),
    );
  }
}

class _Loaded extends StatelessWidget {
  final _Step step;
  final String? error;
  final Function() onNextClicked;

  const _Loaded({required this.step, required this.onNextClicked, this.error});

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
      type: StepperType.vertical,
      currentStep: currentStep,
      controlsBuilder: (context, details) {
        return Padding(
          padding: const EdgeInsets.only(top: 16.0),
          child: Align(
              alignment: Alignment.topLeft,
              child: FilledButton(
                onPressed: onNextClicked,
                child: Text(S.current.onboarding_action_next),
              )),
        );
      },
      steps: [
        Step(
          title: Text(S.current.onboarding_install_ollama_title),
          subtitle: Text(S.current.onboarding_install_ollama_subtitle),
          content: _StepInstallOllama(),
          state: installOllamaState,
          isActive: currentStep >= 0,
        ),
        Step(
          title: Text(S.current.onboarding_configure_cors_title),
          subtitle: Text(S.current.onboarding_configure_cors_subtitle),
          content: _StepConfigureCors(error: error),
          state: setupCorsState,
          isActive: currentStep >= 1,
        ),
        Step(
          title: Text(S.current.onboarding_install_model_title),
          subtitle: Text(S.current.onboarding_install_model_subtitle),
          content: _StepInstallModel(error: error),
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
    return Align(
      alignment: Alignment.topLeft,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(S.current.onboarding_install_ollama_intro),
          const SizedBox(height: 16),
          ElevatedButton(
              onPressed: () => launchUrlString("https://ollama.com/download"),
              child: Text(S.current.onboarding_install_ollama_action)),
          const SizedBox(height: 16),
          Text(S.current.onboarding_install_ollama_outro),
        ],
      ),
    );
  }
}

class _StepConfigureCors extends StatelessWidget {
  final String? error;

  const _StepConfigureCors({this.error});

  @override
  Widget build(BuildContext context) {
    final errorMessage = error;
    return Align(
      alignment: Alignment.topLeft,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(S.current.onboarding_configure_cors_intro),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: () => launchUrlString(
                "https://github.com/Otacon/olpaka/blob/main/docs/setup_cors.md"),
            child: Text(S.current.onboarding_configure_cors_action),
          ),
          const SizedBox(height: 16),
          Text(S.current.onboarding_configure_cors_outro),
          if (errorMessage != null)
            Card.filled(
                color: Theme.of(context).colorScheme.errorContainer,
                child: Text(
                  errorMessage,
                  style: Theme.of(context)
                      .textTheme
                      .bodyMedium
                      ?.copyWith(color: Theme.of(context).colorScheme.onError),
                ))
        ],
      ),
    );
  }
}

class _StepInstallModel extends StatelessWidget {
  final String? error;

  const _StepInstallModel({this.error});

  @override
  Widget build(BuildContext context) {
    final errorMessage = error;
    return Align(
      alignment: Alignment.topLeft,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(S.current.onboarding_install_model_intro),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: () => launchUrlString("https://ollama.com/library"),
            child: Text(S.current.onboarding_install_model_action),
          ),
          const SizedBox(height: 16),
          Text(S.current.onboarding_install_model_outro_1),
          const SizedBox(height: 16),
          MarkdownBlock(
            config: _markdownConfig(context),
            data: "```bash\nollama pull <model_name>\n```",
            selectable: true,
          ),
          const SizedBox(height: 16),
          Text(S.current.onboarding_install_model_outro_2),
          if (errorMessage != null)
          Card.filled(
              color: Theme.of(context).colorScheme.errorContainer,
              child: Text(
                errorMessage,
                style: Theme.of(context)
                    .textTheme
                    .bodyMedium
                    ?.copyWith(color: Theme.of(context).colorScheme.onError),
              ))
        ],
      ),
    );
  }
}

_markdownConfig(BuildContext context) {
  final isDark = Theme.of(context).brightness == Brightness.dark;
  return isDark ? MarkdownConfig.darkConfig : MarkdownConfig.defaultConfig;
}

enum _Step {
  installOllama,
  setupCors,
  installModel,
}
