import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/feature/getting_started/events.dart';
import 'package:olpaka/feature/getting_started/state.dart';
import 'package:olpaka/feature/getting_started/view_model.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';
import 'package:url_launcher/url_launcher_string.dart';

showGettingStartedDialog({
  required BuildContext context,
}) {
  showDialog(
    context: context,
    builder: (_) => const GettingStartedView(),
  );
}

class GettingStartedView extends StatelessWidget {
  const GettingStartedView({super.key});

  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<GettingStartedViewModel>.reactive(
      viewModelBuilder: () => GetIt.I.get(),
      onViewModelReady: (viewModel) {
        viewModel.events.listen((event) {
          switch (event) {
            case CloseGettingStartedEvent():
              Navigator.of(context).pop(false);
            case OpenDownloadOllamaGettingStartedEvent():
              launchUrlString("https://ollama.com/download");
            case OpenSetupCorsGettingStartedEvent():
              launchUrlString(
                  "https://github.com/Otacon/olpaka/blob/main/docs/setup_cors.md");
          }
        });
        viewModel.onCreate();
      },
      builder: (context, viewModel, child) {
        final state = viewModel.state;
        return FractionallySizedBox(
          widthFactor: 0.75,
          child: AlertDialog(
            title: _title(context, state),
            content: _content(
              context,
              state,
              viewModel.onCheckConnectionClicked,
              viewModel.onDownloadOllamaClicked,
              viewModel.onSetupCorsClicked,
            ),
            actions: _actions(
              context,
              state,
              viewModel.onPreviousClicked,
              viewModel.onNextClicked,
              viewModel.onFinishClicked,
            ),
          ),
        );
      },
    );
  }

  Widget _title(
    BuildContext context,
    GettingStartedState state,
  ) {
    return Text(S.current.onboarding_title);
  }

  Widget _content(
    BuildContext context,
    GettingStartedState state,
    Function() onCheckConnectionClicked,
    Function() onDownloadOllamaClicked,
    Function() onSetupCorsClicked,
  ) {
    final currentStep = state.currentStep;
    final Widget stepView;
    switch (currentStep) {
      case 0:
        stepView = _step1();
      case 1:
        stepView = _step2(onDownloadOllamaClicked);
      case 2:
        stepView = _step3(
          onCheckConnectionClicked,
          onSetupCorsClicked,
          state.isConnected,
        );
      default:
        throw UnimplementedError();
    }
    return SingleChildScrollView(child: stepView);
  }

  Widget _step1() {
    return Text(S.current.onboarding_step_1);
  }

  Widget _step2(Function() onDownloadOllamaClicked) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(S.current.onboarding_step_2_a),
        const SizedBox(height: 16),
        FilledButton(
          onPressed: onDownloadOllamaClicked,
          child: Text(S.current.onboarding_download_ollama),
        ),
        const SizedBox(height: 16),
        Text(S.current.onboarding_step_2_b),
      ],
    );
  }

  Widget _step3(
    Function() onCheckConnectionClicked,
    Function() onSetupCorsClicked,
    bool? isConnected,
  ) {
    final String connectionText;
    final IconData connectionIcon;
    if (isConnected == null) {
      connectionIcon = Icons.question_mark;
      connectionText = S.current.onboarding_check_connection_unknown;
    } else if (isConnected) {
      connectionIcon = Icons.check_circle_outline;
      connectionText = S.current.onboarding_check_connection_success;
    } else {
      connectionIcon = Icons.warning_amber;
      connectionText = S.current.onboarding_check_connection_failure;
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(S.current.onboarding_step_3_a),
        const SizedBox(height: 16),
        FilledButton(
          onPressed: onSetupCorsClicked,
          child: Text(S.current.onboarding_setup_cors),
        ),
        const SizedBox(height: 16),
        Text(S.current.onboarding_step_3_b),
        const SizedBox(height: 16),
        FilledButton.icon(
          onPressed: onCheckConnectionClicked,
          icon: Icon(connectionIcon),
          label: Text(connectionText),
        ),
        const SizedBox(height: 16),
        Text(S.current.onboarding_step_3_c),
      ],
    );
  }

  List<Widget> _actions(
    BuildContext context,
    GettingStartedState state,
    Function() onPreviousClicked,
    Function() onNextClicked,
    Function() onFinishClicked,
  ) {
    return [
      if (state.showPrevious)
        TextButton(
          onPressed: onPreviousClicked,
          child: const Text("Previous"),
        ),
      if (state.isLastStep)
        FilledButton(
          onPressed: onFinishClicked,
          child: const Text("Finish"),
        )
      else
        FilledButton(
          onPressed: onNextClicked,
          child: const Text("Next"),
        ),
    ];
  }
}
