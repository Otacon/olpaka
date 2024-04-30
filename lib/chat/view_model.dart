import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:olpaka/ollama/repository.dart';

class ChatViewModel with ChangeNotifier {
  final OllamaRepository _repository;

  ChatState state = ChatState(
    isLoading: true,
    selectedModel: null,
    models: List.empty(),
    messages: List.empty(),
  );

  final _events = StreamController<ChatEvent>.broadcast();

  Stream<ChatEvent> get events => _events.stream.map((val) => val);

  ChatViewModel(this._repository);

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
            "Error",
            "Looks like something went wrong...Maybe try to restart Olpaka.",
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
              "Missing Models",
              "You've got no AI models installed.\nDownload a model by running `ollama run llama3`. Visit https://ollama.com/library to find more.",
              "Done",
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
      case ListModelResultConnectionError():
        state = ChatState(
          isLoading: false,
          selectedModel: state.selectedModel,
          models: state.models,
          messages: List.empty(),
        );
        _events.add(
          OllamaNotFound(
            "Ollama not found",
            "Looks like Ollama is not installed. Visit https://ollama.com/download to install it and try again.",
            "Done",
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
            "Error",
            "Looks like something went wrong...Maybe try to restart Olpaka.",
          ),
        );
    }
    notifyListeners();
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
