import 'dart:async';

import 'package:olpaka/core/ollama/model.dart';
import 'package:olpaka/core/ollama/repository.dart';
import 'package:olpaka/core/state/chat/chat_message_domain.dart';
import 'package:olpaka/core/state/chat/chat_state_holder.dart';
import 'package:olpaka/core/state/chat/send_message_result.dart';
import 'package:olpaka/core/state/models/model_domain.dart';
import 'package:olpaka/core/state/models/model_state_holder.dart';
import 'package:olpaka/feature/chat/events.dart';
import 'package:olpaka/feature/chat/state.dart';
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
    final result = await _chatState.sendMessage(message, selectedModel.id);
    switch (result) {
      case SendMessageResultSuccess():
        break;
      case SendMessageResultError():
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
      messages: state.messages,
    );
    notifyListeners();
  }

  _load() async {
    final response = await _modelsState.refresh();
    final messages =
        _chatState.messages.value.map(_domainToChatMessage).toList();
    final List<ChatModel> models;
    switch (response) {
      case ListModelsResultSuccess():
        models = response.models.map(_modelToChatModel).toList();
        if (models.isEmpty) {
          _events.add(
            ModelNotFound(
              _s.chat_missing_model_dialog_title,
              _s.chat_missing_model_dialog_message,
              _s.chat_missing_model_dialog_positive,
            ),
          );
        }
      case ListModelResultConnectionError():
        models = state.models;
        _events.add(
          OllamaNotFound(
            _s.chat_missing_ollama_dialog_title,
            _s.chat_missing_ollama_dialog_message,
            _s.chat_missing_ollama_dialog_positive,
          ),
        );
      case ListModelResultError():
        models = state.models;
        _events.add(
          GenericError(
            _s.error_generic_title,
            _s.error_generic_message,
          ),
        );
    }
    state = ChatState(
      isLoading: false,
      selectedModel: models.firstOrNull,
      models: models,
      messages: messages,
    );
    notifyListeners();
    _modelsState.cachedModels.addListener(_onModelsChanged);
    _chatState.messages.addListener(_onChatChanged);
  }

  _onModelsChanged() {
    final uiModels =
        _modelsState.cachedModels.value.map(_domainToChatModel).toList();
    state = ChatState(
      isLoading: state.isLoading,
      selectedModel: state.selectedModel,
      models: uiModels,
      messages: state.messages,
    );
    notifyListeners();
  }

  _onChatChanged() {
    final messages =
        _chatState.messages.value.map(_domainToChatMessage).toList();
    state = ChatState(
      isLoading: state.isLoading,
      selectedModel: state.selectedModel,
      models: state.models,
      messages: messages,
    );
    notifyListeners();
  }

  ChatModel _domainToChatModel(ModelDomainAvailable model) {
    return ChatModel(model.id, model.name);
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
    _modelsState.cachedModels.removeListener(_onModelsChanged);
    _chatState.messages.removeListener(_onChatChanged);
  }
}
