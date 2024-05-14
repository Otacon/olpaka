import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/feature/models/events.dart';
import 'package:olpaka/feature/models/state.dart';
import 'package:olpaka/feature/models/view_model.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:olpaka/ui/empty_screen.dart';
import 'package:olpaka/ui/loading.dart';
import 'package:olpaka/ui/markdown.dart';
import 'package:stacked/stacked.dart';

class ModelsScreen extends StatelessWidget {
  const ModelsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<ModelsViewModel>.reactive(
      viewModelBuilder: () => GetIt.I.get(),
      onViewModelReady: (viewModel) {
        viewModel.events.listen((event) {
          switch (event) {
            case ModelsEventShowAddModelDialog():
              _showDownloadModelDialog(
                context: context,
                positiveAction: (modelName) => viewModel.addModel(modelName),
              );
            case ModelsEventShowError():
              _showErrorDialog(
                context: context,
                title: event.title,
                message: event.message,
              );
            case ModelsEventShowRemoveModelDialog():
              _showRemoveModelDialog(
                context: context,
                modelName: event.modelName,
                modelId: event.modelId,
                positiveAction: viewModel.onConfirmRemoveModel,
              );
          }
        });
        viewModel.onCreate();
      },
      builder: (context, viewModel, child) {
        final state = viewModel.state;
        final Widget content;
        final Widget? fab;
        switch (state) {
          case ModelsStateLoading():
            content = const Loading();
            fab = null;
          case ModelsStateContent():
            content = _content(
              state.models,
              viewModel.onRemoveModel,
            );
            fab = FloatingActionButton(
              onPressed: viewModel.onAddModelClicked,
              child: const Icon(Icons.add),
            );
          case ModelsStateError():
            final String ctaText = state.ctaText ?? "";
            final Function()? onCtaClicked;
            if (state.ctaText != null) {
              onCtaClicked = viewModel.onRefreshClicked;
            } else {
              onCtaClicked = null;
            }
            content = EmptyScreen(
              header: state.title,
              body: state.message,
              ctaText: ctaText,
              onCtaClicked: onCtaClicked,
            );
            fab = null;
        }
        return Scaffold(
            appBar: AppBar(
              elevation: 4,
              shadowColor: Theme.of(context).shadowColor,
              centerTitle: true,
              actions: [
                IconButton(
                  onPressed: viewModel.onRefreshClicked,
                  icon: const Icon(Icons.refresh),
                ),
              ],
              title: Text(
                S.current.models_title,
                style: Theme.of(context).textTheme.headlineMedium,
              ),
            ),
            body: content,
            floatingActionButton: fab);
      },
    );
  }

  _content(
    List<ModelItem> models,
    Function(ModelItem) onRemoveModel,
  ) {
    return Column(
      children: [
        Expanded(
          child: ListView.builder(
            itemCount: models.length,
            itemBuilder: (context, index) {
              final model = models[index];
              if (model.isLoading) {
                return _modelItemDownloading(context, model);
              } else {
                return _modelItemAvailable(context, model, onRemoveModel);
              }
            },
          ),
        ),
      ],
    );
  }

  _modelItemAvailable(
    BuildContext context,
    ModelItem model,
    Function(ModelItem) onRemoveModel,
  ) {
    return Column(
      children: [
        ListTile(
          title: Text(model.title),
          subtitle: Text(model.subtitle),
          leading: const Icon(Icons.storage),
          trailing: FilledButton(
            onPressed: () => onRemoveModel(model),
            child: Text(S.current.models_action_remove_model),
          ),
          tileColor: Theme.of(context).colorScheme.surface,
        ),
        const Divider(
          height: 0,
        )
      ],
    );
  }

  Widget _modelItemDownloading(BuildContext context, ModelItem model) {
    final Widget? percentageWidget;
    final doubleProgress = model.progress;
    final theme = Theme.of(context);
    if (doubleProgress != null) {
      int percentage = (doubleProgress * 100).round();
      percentageWidget = Text(
        "$percentage",
        style: theme.textTheme.labelSmall,
      );
    } else {
      percentageWidget = null;
    }

    return Column(
      children: [
        ListTile(
          title: Text(model.title),
          subtitle: Text(model.subtitle),
          leading: SizedBox(
            width: 24,
            height: 24,
            child: Stack(
              alignment: AlignmentDirectional.center,
              children: [
                CircularProgressIndicator(value: doubleProgress),
                if (percentageWidget != null) percentageWidget
              ],
            ),
          ),
          tileColor: theme.colorScheme.surface,
        ),
        const Divider(
          height: 0,
        )
      ],
    );
  }

  _showErrorDialog({
    required BuildContext context,
    required String title,
    required String message,
  }) {
    showDialog(
      barrierDismissible: false,
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text(title),
          content: Text(message),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop(false);
              },
              child: Text(S.current.error_generic_positive),
            ),
          ],
        );
      },
    );
  }

  _showDownloadModelDialog({
    required BuildContext context,
    required Function(String) positiveAction,
  }) {
    showDialog(
      barrierDismissible: false,
      context: context,
      builder: (context) {
        return _DownloadModelDialog(positiveAction);
      },
    );
  }

  _showRemoveModelDialog({
    required BuildContext context,
    required String modelName,
    required String modelId,
    required Function(String) positiveAction,
  }) {
    showDialog(
      barrierDismissible: false,
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text(S.current.models_dialog_remove_model_title),
          content:
              Text(S.current.models_dialog_remove_model_description(modelName)),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(false),
              child: Text(S.current.models_dialog_remove_model_action_negative),
            ),
            FilledButton(
              onPressed: () {
                Navigator.of(context).pop(false);
                positiveAction(modelId);
              },
              child: Text(S.current.models_dialog_remove_model_action_positive),
            )
          ],
        );
      },
    );
  }
}

class _DownloadModelDialog extends StatefulWidget {
  final Function(String) positiveAction;

  const _DownloadModelDialog(this.positiveAction);

  @override
  State<StatefulWidget> createState() {
    return _DownloadModelDialogState();
  }
}

class _DownloadModelDialogState extends State<_DownloadModelDialog> {
  final TextEditingController _controller = TextEditingController();
  bool submitEnabled = false;

  @override
  void initState() {
    _controller.addListener(() {
      setState(() => submitEnabled = _controller.text.isNotEmpty);
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final Function(String)? submitCallback;
    final Function()? positiveButtonCallback;
    if (submitEnabled) {
      submitCallback = (modelName) => _onSubmit(context, modelName);
      positiveButtonCallback = () => _onSubmit(context, _controller.text);
    } else {
      submitCallback = null;
      positiveButtonCallback = null;
    }

    return AlertDialog(
      title: Text(S.current.models_dialog_download_model_title),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Markdown(S.current.models_dialog_download_model_description),
          const SizedBox(height: 16),
          TextField(
            controller: _controller,
            onSubmitted: submitCallback,
            decoration: InputDecoration(
              border: const OutlineInputBorder(),
              hintText: S.current.models_dialog_download_model_text_hint,
            ),
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(false),
          child: Text(S.current.models_dialog_download_model_action_negative),
        ),
        FilledButton(
          onPressed: positiveButtonCallback,
          child: Text(S.current.models_dialog_download_model_action_positive),
        )
      ],
    );
  }

  _onSubmit(BuildContext context, String modelName) {
    Navigator.of(context).pop(false);
    widget.positiveAction(modelName);
    _controller.clear();
  }
}
