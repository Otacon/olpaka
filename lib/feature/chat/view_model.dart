import 'dart:async';

import 'package:olpaka/core/analytics/analytics.dart';
import 'package:olpaka/core/analytics/event.dart';
import 'package:olpaka/core/analytics/screen_view.dart';
import 'package:olpaka/core/ollama/list_models_result.dart';
import 'package:olpaka/core/state/chat/chat_message_domain.dart';
import 'package:olpaka/core/state/chat/chat_state_holder.dart';
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
  final Analytics _analytics;

  ChatViewModel(this._modelsState, this._chatState, this._s, this._analytics);

  ChatState state = ChatStateLoading();

  final _events = StreamController<ChatEvent>.broadcast();

  Stream<ChatEvent> get events => _events.stream.map((val) => val);

  onCreate() async {
    _analytics.screenView(ScreenViewChat());
    _modelsState.cachedModels.addListener(_onModelsChanged);
    _chatState.messages.addListener(_onChatChanged);
    await _load();
  }

  onRefresh() async {
    await _load();
  }

  onModelChanged(ChatModel? model) async {
    if (model == null) {
      return;
    }

    final currentState = state;
    switch (currentState) {
      case ChatStateLoading():
      case ChatStateError():
        return;
      case ChatStateContent():
    }

    state = ChatStateContent(
      selectedModel: model,
      models: _getModels(),
      messages: _getMessages(),
      isGeneratingMessage: currentState.isGeneratingMessage,
    );
    notifyListeners();
  }

  onSendMessage(String message) async {
    final currentState = state;
    switch (currentState) {
      case ChatStateError():
      case ChatStateLoading():
        return;
      case ChatStateContent():
    }

    state = ChatStateContent(
      selectedModel: currentState.selectedModel,
      models: _getModels(),
      messages: _getMessages(),
      isGeneratingMessage: true,
    );
    notifyListeners();
    _analytics.event(EventSendMessage(currentState.selectedModel.id));
    await _chatState.sendMessage(message, currentState.selectedModel.id);
    state = ChatStateContent(
      selectedModel: currentState.selectedModel,
      models: _getModels(),
      messages: _getMessages(),
      isGeneratingMessage: false,
    );
    notifyListeners();
  }

  _load() async {
    state = ChatStateLoading();
    notifyListeners();
    final response = await _modelsState.refresh();
    switch (response) {
      case ListModelResultError():
      case ListModelResultConnectionError():
        state = ChatStateError(
          _s.error_missing_ollama_title,
          _s.error_missing_ollama_message,
          _s.error_missing_ollama_positive,
        );
        notifyListeners();
        return;
      case ListModelsResultSuccess():
    }
    final models = _getModels();
    if (models.isEmpty) {
      state = ChatStateError(
        _s.chat_missing_model_error_title,
        _s.chat_missing_model_error_message,
        _s.chat_missing_model_error_positive,
      );
    } else {
      state = ChatStateContent(
        isGeneratingMessage: false,
        selectedModel: models.first,
        models: models,
        messages: _getMessages(),
      );
    }
    notifyListeners();
  }

  _onModelsChanged() {
    final models = _getModels();
    final currentState = state;
    switch (currentState) {
      case ChatStateError():
      case ChatStateLoading():
        if (models.isEmpty) {
          state = ChatStateError(
            _s.chat_missing_model_error_title,
            _s.chat_missing_model_error_message,
            _s.chat_missing_model_error_positive,
          );
        } else {
          state = ChatStateContent(
            isGeneratingMessage: false,
            selectedModel: models.first,
            models: models,
            messages: _getMessages(),
          );
        }
      case ChatStateContent():
        state = ChatStateContent(
          selectedModel: currentState.selectedModel,
          models: models,
          messages: _getMessages(),
          isGeneratingMessage: currentState.isGeneratingMessage,
        );
    }
    notifyListeners();
  }

  _onChatChanged() {
    final currentState = state;
    switch (currentState) {
      case ChatStateLoading():
      case ChatStateError():
        return;
      case ChatStateContent():
    }
    state = ChatStateContent(
      isGeneratingMessage: currentState.isGeneratingMessage,
      selectedModel: currentState.selectedModel,
      models: _getModels(),
      messages: _getMessages(),
    );
    notifyListeners();
  }

  List<ChatModel> _getModels() {
    return _modelsState.cachedModels.value.map(_domainToChatModel).toList();
  }

  List<ChatMessage> _getMessages() {
    return _chatState.messages.value.map(_domainToChatMessage).toList();
  }

  ChatModel _domainToChatModel(ModelDomainAvailable model) {
    return ChatModel(model.id, model.name);
  }

  ChatMessage _domainToChatMessage(ChatMessageDomain message) {
    final ChatMessage chatMessage;
    switch (message) {
      case ChatMessageUserDomain():
        chatMessage = ChatMessageUser(message.message);
      case ChatMessageAssistantDomain():
        chatMessage = ChatMessageAssistant(message.message,
            isLoading: !message.isFinalised);
      case ChatMessageErrorDomain():
        chatMessage = ChatMessageError(_s.chat_message_error);
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
