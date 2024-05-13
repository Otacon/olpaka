import 'dart:async';

import 'package:olpaka/core/ollama/repository.dart';
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

  ChatViewModel(this._modelsState, this._chatState, this._s);

  ChatState state = ChatStateLoading();

  final _events = StreamController<ChatEvent>.broadcast();

  Stream<ChatEvent> get events => _events.stream.map((val) => val);

  onCreate() async {
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
    final response = await _modelsState.refresh();
    switch (response) {
      case ListModelResultError():
      case ListModelResultConnectionError():
        //TODO trigger error
        return;
      case ListModelsResultSuccess():
    }
    final models = _getModels();
    if(models.isEmpty){
      //TODO handle empty models
    }
    state = ChatStateContent(
      isGeneratingMessage: false,
      selectedModel: models.first,
      models: models,
      messages: _getMessages(),
    );
    notifyListeners();
    return true;
  }

  _onModelsChanged() {
    final currentState = state;
    switch(currentState){
      case ChatStateError():
      case ChatStateLoading():
        return;
      case ChatStateContent():
    }
    final models = _getModels();
    //TODO handle selected model
    state = ChatStateContent(
      selectedModel: currentState.selectedModel,
      models : models,
      messages: _getMessages(),
      isGeneratingMessage: currentState.isGeneratingMessage
    );
    notifyListeners();
  }

  _onChatChanged() {
    final currentState = state;
    switch(currentState){
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

  List<ChatModel> _getModels(){
    return _modelsState.cachedModels.value.map(_domainToChatModel).toList();
  }

  List<ChatMessage> _getMessages(){
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
