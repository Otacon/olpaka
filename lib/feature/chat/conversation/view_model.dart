import 'dart:async';

import 'package:olpaka/core/analytics/analytics.dart';
import 'package:olpaka/core/analytics/event.dart';
import 'package:olpaka/core/analytics/screen_view.dart';
import 'package:olpaka/core/ollama/list_models_result.dart';
import 'package:olpaka/core/state/chat/chat_message_domain.dart';
import 'package:olpaka/core/state/chat/chat_state_holder.dart';
import 'package:olpaka/core/state/models/model_domain.dart';
import 'package:olpaka/core/state/models/model_state_holder.dart';
import 'package:olpaka/feature/chat/conversation/events.dart';
import 'package:olpaka/feature/chat/conversation/state.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:stacked/stacked.dart';

class ConversationViewModel extends BaseViewModel {
  final ModelStateHolder _modelsState;
  final ChatStateHolder _chatState;
  final S _s;
  final Analytics _analytics;

  ConversationViewModel(this._modelsState, this._chatState, this._s, this._analytics);

  ConversationState state = ConversationStateLoading();

  final _events = StreamController<ConversationEvent>.broadcast();

  Stream<ConversationEvent> get events => _events.stream.map((val) => val);

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
      case ConversationStateLoading():
      case ConversationStateError():
        return;
      case ConversationStateContent():
    }

    state = ConversationStateContent(
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
      case ConversationStateError():
      case ConversationStateLoading():
        return;
      case ConversationStateContent():
    }

    state = ConversationStateContent(
      selectedModel: currentState.selectedModel,
      models: _getModels(),
      messages: _getMessages(),
      isGeneratingMessage: true,
    );
    notifyListeners();
    _analytics.event(EventSendMessage(currentState.selectedModel.id));
    await _chatState.sendMessage(message, currentState.selectedModel.id);
    state = ConversationStateContent(
      selectedModel: currentState.selectedModel,
      models: _getModels(),
      messages: _getMessages(),
      isGeneratingMessage: false,
    );
    notifyListeners();
  }

  _load() async {
    state = ConversationStateLoading();
    notifyListeners();
    final response = await _modelsState.refresh();
    switch (response) {
      case ListModelResultError():
      case ListModelResultConnectionError():
        state = ConversationStateError(
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
      state = ConversationStateError(
        _s.chat_missing_model_error_title,
        _s.chat_missing_model_error_message,
        _s.chat_missing_model_error_positive,
      );
    } else {
      state = ConversationStateContent(
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
      case ConversationStateError():
      case ConversationStateLoading():
        if (models.isEmpty) {
          state = ConversationStateError(
            _s.chat_missing_model_error_title,
            _s.chat_missing_model_error_message,
            _s.chat_missing_model_error_positive,
          );
        } else {
          state = ConversationStateContent(
            isGeneratingMessage: false,
            selectedModel: models.first,
            models: models,
            messages: _getMessages(),
          );
        }
      case ConversationStateContent():
        state = ConversationStateContent(
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
      case ConversationStateLoading():
      case ConversationStateError():
        return;
      case ConversationStateContent():
    }
    state = ConversationStateContent(
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

  List<ConversationMessage> _getMessages() {
    return _chatState.messages.value.map(_domainToChatMessage).toList();
  }

  ChatModel _domainToChatModel(ModelDomainAvailable model) {
    return ChatModel(model.id, model.name);
  }

  ConversationMessage _domainToChatMessage(ChatMessageDomain message) {
    final ConversationMessage chatMessage;
    switch (message) {
      case ChatMessageUserDomain():
        chatMessage = ConversationMessageUser(message.message);
      case ChatMessageAssistantDomain():
        chatMessage = ConversationMessageAssistant(message.message,
            isLoading: !message.isFinalised);
      case ChatMessageErrorDomain():
        chatMessage = ConversationMessageError(_s.chat_message_error);
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
