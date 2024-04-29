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
    final response = await _repository.listModels();
    switch (response) {
      case ListModelsResultSuccess():
        final modelNames = response.models.map((model) => model.name).toList();
        //TODO handle no models
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
          ShowError(
            "Ollama not found",
            "Looks like Ollama is not installed. Please install it and then reopen Olpaka.",
            "Install Ollama",
            negative: "Cancel",
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
          ShowError(
            "Error",
            "Looks like something went wrong...Maybe try to restart Olpaka.",
            "Ok",
          ),
        );
    }
    notifyListeners();
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
    switch(result){
      case GenerateResultSuccess():
        newMessages.add(ChatMessage(
          isUser: false,
          message: result.answer,
          isLoading: false,
        ));
      case GenerateResultError():
        _events.add(
          ShowError(
            "Error",
            "Looks like something went wrong...Maybe try to restart Olpaka.",
            "Ok",
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

class ShowError extends ChatEvent {
  final String title;
  final String message;
  final String positive;
  final String? negative;

  ShowError(this.title, this.message, this.positive,{this.negative});
}
