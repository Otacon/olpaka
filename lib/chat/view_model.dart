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

  ChatViewModel(this._repository);

  onCreate() async {
    final models =
        (await _repository.listModels()).map((model) => model.name).toList();

    final selectedModel = models.firstOrNull;

    state = ChatState(
      isLoading: false,
      selectedModel: selectedModel,
      models: models,
      messages: List.empty(),
    );
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
    final ownMessage = ChatMessage(true, message);
    final newMessages = List<ChatMessage>.from(state.messages, growable: true);
    newMessages.add(ownMessage);
    state = ChatState(
      isLoading: false,
      selectedModel: state.selectedModel,
      models: state.models,
      messages: newMessages,
    );
    notifyListeners();
    final answer = await _repository.generate(state.selectedModel!, message);
    final reply = ChatMessage(false, answer);
    newMessages.add(reply);
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

  ChatMessage(this.isUser, this.message);
}
