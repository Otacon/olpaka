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
          title: const Text("Download new Model"),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text("Pick a model from ollama library and add its name here"),
              const SizedBox(height: 16),
              TextField(
                controller: _controller,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  hintText: "Add model name",
                ),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(false),
              child: const Text("Cancel"),
            ),
            FilledButton(
              onPressed: () => {
                Navigator.of(context).pop(false),
                positiveAction(_controller.value.text)
              },
              child: const Text("Download"),
            )
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
                if(model.isLoading){
                  leading = const Icon(Icons.downloading_outlined);
                  trailing = null;
                } else {
                  leading = const Icon(Icons.download_done);
                  trailing = IconButton(
                    onPressed: () => onRemoveModel(model.name),
                    icon: const Icon(Icons.delete),
                  );
                }
                return Column(
                  children: [
                    ListTile(
                      title: Text("${model.name} (${model.fullName})"),
                      subtitle: Text("${model.size} - ${model.params} - ${model.quantization}"),
                      leading: leading,
                      trailing: trailing,
                      tileColor: Theme.of(context).colorScheme.surface,
                    ),
                    const Divider(
                      height: 0,
                    )
                  ],
                );
              }),
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
