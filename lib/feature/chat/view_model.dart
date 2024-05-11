import 'dart:async';

import 'package:olpaka/core/ollama/model.dart';
import 'package:olpaka/core/ollama/repository.dart';
import 'package:olpaka/core/state/chat_state_holder.dart';
import 'package:olpaka/core/state/model_state_holder.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class ChatViewModel extends BaseViewModel {
  final ModelStateHolder _modelsState;
  final ChatStateHolder _chatState;
  final S _s;

  ChatViewModel(this._modelsState, this._chatState, this._s);

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

  onModelChanged(ChatModel? model) async {
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
    final selectedModel = state.selectedModel;
    if (selectedModel == null) {
      return;
    }
    state = ChatState(
      isLoading: true,
      selectedModel: state.selectedModel,
      models: state.models,
      messages: state.messages,
    );
    notifyListeners();
    final result = _chatState.sendMessageStreaming(message, selectedModel.id);
    //TODO handle result
    state = ChatState(
      isLoading: false,
      selectedModel: state.selectedModel,
      models: state.models,
      messages: state.messages,
    );
    notifyListeners();
  }

  _load() async {
    //TODO tweak this part.
    final response = await _modelsState.refresh();
    final messages = _chatState.messages.map(_domainToChatMessage).toList();
    switch (response) {
      case ListModelsResultSuccess():
        final models = response.models.map(_modelToChatModel).toList();
        if (models.isEmpty) {
          _events.add(
            ModelNotFound(
              _s.chat_missing_model_dialog_title,
              _s.chat_missing_model_dialog_message,
              _s.chat_missing_model_dialog_positive,
            ),
          );
        }
        final selectedModel = models.firstOrNull;
        state = ChatState(
          isLoading: false,
          selectedModel: selectedModel,
          models: models,
          messages: messages,
        );
        _modelsState.addListener(_onModelsChanged);
        _chatState.addListener(_onChatChanged);
      case ListModelResultConnectionError():
        state = ChatState(
          isLoading: false,
          selectedModel: state.selectedModel,
          models: state.models,
          messages: messages,
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
          messages: messages,
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

  _onModelsChanged() {
    final uiModels = _modelsState.cachedModels.map(_domainToChatModel).toList();
    state = ChatState(
      isLoading: state.isLoading,
      selectedModel: state.selectedModel,
      models: uiModels,
      messages: state.messages,
    );
    notifyListeners();
  }

  _onChatChanged() {
    final messages = _chatState.messages.map(_domainToChatMessage).toList();
    state = ChatState(
      isLoading: state.isLoading,
      selectedModel: state.selectedModel,
      models: state.models,
      messages: messages,
    );
    notifyListeners();
  }

  ChatModel _domainToChatModel(ModelDomain model) {
    return ChatModel(model.fullName, model.name);
  }

  ChatModel _modelToChatModel(Model model) {
    // TODO map to domain in the layer below.
    return ChatModel(model.model, model.name);
  }

  ChatMessage _domainToChatMessage(ChatMessageDomain message) {
    final ChatMessage chatMessage;
    switch (message) {
      case ChatMessageUserDomain():
        chatMessage = ChatMessage(
            isUser: true, message: message.message, isLoading: false);
      case ChatMessageAssistantDomain():
        chatMessage = ChatMessage(
            isUser: false,
            message: message.message,
            isLoading: message.isFinalised);
    }
    return chatMessage;
  }

  @override
  void dispose() {
    super.dispose();
    _modelsState.removeListener(_onModelsChanged);
    _chatState.removeListener(_onChatChanged);
  }
}

class ChatState {
  final bool isLoading;
  final ChatModel? selectedModel;
  final List<ChatModel> models;
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

class ChatModel {
  final String id;
  final String name;

  ChatModel(this.id, this.name);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ChatModel &&
          runtimeType == other.runtimeType &&
          id == other.id &&
          name == other.name;

  @override
  int get hashCode => id.hashCode ^ name.hashCode;
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
