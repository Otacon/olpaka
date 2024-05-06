import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:olpaka/models/view_model.dart';
import 'package:stacked/stacked.dart';

class ModelsScreen extends StatelessWidget {
  ModelsScreen({super.key});

  final TextEditingController _controller = TextEditingController();

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
          }
        });
        viewModel.onCreate();
      },
      builder: (context, viewModel, child) {
        final state = viewModel.state;
        final content = switch (state) {
          ModelsStateLoading() => _Loading(),
          ModelsStateLoaded() => _Content(
              models: state.models,
              onRemoveModel: viewModel.removeModel,
            ),
        };
        return Scaffold(
          appBar: AppBar(
            elevation: 4,
            shadowColor: Theme.of(context).shadowColor,
            centerTitle: true,
            title: Text(
              S.current.models_title,
              style: Theme.of(context).textTheme.headlineMedium,
            ),
          ),
          body: content,
          floatingActionButton: FloatingActionButton(
              onPressed: viewModel.onAddModelClicked,
              child: const Icon(Icons.add)),
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
        return AlertDialog(
          title: Text(S.current.models_dialog_download_model_title),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(S.current.models_dialog_download_model_description),
              const SizedBox(height: 16),
              TextField(
                controller: _controller,
                decoration: InputDecoration(
                  border: const OutlineInputBorder(),
                  hintText: S.current.models_dialog_download_model_text_hint,
                ),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop(false);
                _controller.clear();
              },
              child:
                  Text(S.current.models_dialog_download_model_action_negative),
            ),
            FilledButton(
              onPressed: () {
                Navigator.of(context).pop(false);
                positiveAction(_controller.value.text);
                _controller.clear();
              },
              child:
                  Text(S.current.models_dialog_download_model_action_positive),
            )
          ],
        );
      },
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
}

class _Content extends StatelessWidget {
  const _Content({
    required this.models,
    required this.onRemoveModel,
  });

  final List<ModelItem> models;
  final Function(String) onRemoveModel;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Expanded(
          child: ListView.builder(
            itemCount: models.length,
            itemBuilder: (context, index) {
              final model = models[index];
              final Widget leading;
              final Widget? trailing;
              if (model.isLoading) {
                leading = const SizedBox(
                    width: 24,
                    height: 24,
                    child: CircularProgressIndicator()
                );
                trailing = null;
              } else {
                leading = const Icon(Icons.download_done);
                trailing = IconButton(
                  onPressed: () => onRemoveModel(model.id),
                  icon: const Icon(Icons.delete),
                );
              }
              return Column(
                children: [
                  ListTile(
                    title: Text(model.title),
                    subtitle: Text(model.subtitle),
                    leading: leading,
                    trailing: trailing,
                    tileColor: Theme.of(context).colorScheme.surface,
                  ),
                  const Divider(
                    height: 0,
                  )
                ],
              );
            },
          ),
        ),
      ],
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
