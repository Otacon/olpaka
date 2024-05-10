import 'dart:async';

import 'package:olpaka/core/ollama/model.dart';
import 'package:olpaka/core/ollama/repository.dart';
import 'package:olpaka/core/state/model_state_holder.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class ChatViewModel extends BaseViewModel {
  final ModelStateHolder _modelManager;
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
    if(selectedModel == null){
      return;
    }
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
    final result = await _repository.generate(selectedModel.id, message);
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
    //TODO tweak this part.
    final response = await _modelManager.refresh();
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
    final uiModels = _modelManager.cachedModels.map(_domainToChatModel).toList();
    state = ChatState(
      isLoading: state.isLoading,
      selectedModel: state.selectedModel,
      models: uiModels,
      messages: state.messages,
    );
    notifyListeners();
  }

  ChatModel _domainToChatModel(ModelDomain model){
    return ChatModel(model.fullName, model.name);
  }

  ChatModel _modelToChatModel(Model model){
    // TODO map to domain in the layer below.
    return ChatModel(model.model, model.name);
  }

  @override
  void dispose() {
    super.dispose();
    _modelManager.removeListener(_onModelsChanged);
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

class ChatModel{
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
