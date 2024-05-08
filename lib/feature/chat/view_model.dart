import 'dart:async';

import 'package:olpaka/core/ollama/repository.dart';
import 'package:olpaka/core/state/model_manager.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class ChatViewModel extends BaseViewModel {
  final ModelManager _modelManager;
  final OllamaRepository _repository;
  final S _s;

  ChatViewModel(this._repository, this._modelManager, this._s, );

  ChatState state = ChatState(
    isLoading: true,
    selectedModel: null,
    models: List.empty(),
    messages: List.empty(),
  );

  final _events = StreamController<ChatEvent>.broadcast();

  Stream<ChatEvent> get events => _events.stream.map((val) => val);

  onCreate() async {
    await _load();
  }

  onRefresh() async {
    await _load();
  }

  onModelChanged(String? model) async {
    if (model == null) {
      return;
    }
    state = ChatState(
      isLoading: false,
      selectedModel: model,
      models: state.models,
      messages: state.messages,
    );
    notifyListeners();
  }

  onSendMessage(String message) async {
    final newMessages = List<ChatMessage>.from(state.messages, growable: true);
    newMessages.add(ChatMessage(isUser: true, message: message));
    var assistantMessage = ChatMessage(isUser: false, isLoading: true);
    newMessages.add(assistantMessage);
    state = ChatState(
      isLoading: true,
      selectedModel: state.selectedModel,
      models: state.models,
      messages: newMessages,
    );
    notifyListeners();
    final result = await _repository.generate(state.selectedModel!, message);
    newMessages.remove(assistantMessage);
    switch (result) {
      case GenerateResultSuccess():
        newMessages.add(ChatMessage(
          isUser: false,
          message: result.answer,
          isLoading: false,
        ));
      case GenerateResultError():
        _events.add(
          GenericError(
            _s.error_generic_title,
            _s.error_generic_message,
          ),
        );
    }
    state = ChatState(
      isLoading: false,
      selectedModel: state.selectedModel,
      models: state.models,
      messages: newMessages,
    );
    notifyListeners();
  }

  _load() async {
    final response = await _repository.listModels();
    switch (response) {
      case ListModelsResultSuccess():
        final modelNames = response.models.map((model) => model.name).toList();
        if (modelNames.isEmpty) {
          _events.add(
            ModelNotFound(
              _s.chat_missing_model_dialog_title,
              _s.chat_missing_model_dialog_message,
              _s.chat_missing_model_dialog_positive,
            ),
          );
        }
        final selectedModel = modelNames.firstOrNull;
        state = ChatState(
          isLoading: false,
          selectedModel: selectedModel,
          models: modelNames,
          messages: List.empty(),
        );
        _modelManager.addListener(_onModelsChanged);
      case ListModelResultConnectionError():
        state = ChatState(
          isLoading: false,
          selectedModel: state.selectedModel,
          models: state.models,
          messages: List.empty(),
        );
        _events.add(
          OllamaNotFound(
            _s.chat_missing_ollama_dialog_title,
            _s.chat_missing_ollama_dialog_message,
            _s.chat_missing_ollama_dialog_positive,
          ),
        );
      case ListModelResultError():
        state = ChatState(
          isLoading: false,
          selectedModel: state.selectedModel,
          models: state.models,
          messages: List.empty(),
        );
        _events.add(
          GenericError(
            _s.error_generic_title,
            _s.error_generic_message,
          ),
        );
    }
    notifyListeners();
  }

  _onModelsChanged(){
    final modelNames = _modelManager.cachedModels.map((model) => model.name).toList();
    state = ChatState(
      isLoading: state.isLoading,
      selectedModel: state.selectedModel,
      models: modelNames,
      messages: state.messages,
    );
  }

  @override
  void dispose() {
    super.dispose();
    _modelManager.removeListener(_onModelsChanged);
  }
}

class ChatState {
  final bool isLoading;
  final String? selectedModel;
  final List<String> models;
  final List<ChatMessage> messages;

  ChatState({
    required this.isLoading,
    required this.selectedModel,
    required this.models,
    required this.messages,
  });
}

class ChatMessage {
  final bool isUser;
  final String message;
  final bool isLoading;

  ChatMessage({
    required this.isUser,
    this.message = "",
    this.isLoading = false,
  });
}

sealed class ChatEvent {}

class GenericError extends ChatEvent {
  final String title;
  final String message;

  GenericError(this.title, this.message);
}

class OllamaNotFound extends ChatEvent {
  final String title;
  final String message;
  final String positive;

  OllamaNotFound(this.title, this.message, this.positive);
}

class ModelNotFound extends ChatEvent {
  final String title;
  final String message;
  final String positive;

  ModelNotFound(this.title, this.message, this.positive);
}
