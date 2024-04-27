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
    const prompt = "Could you please generate a message with all the formattings of markdown?!";
    final ownMessage = ChatMessage(true, prompt);
    state = ChatState(
      isLoading: false,
      selectedModel: selectedModel,
      models: models,
      messages: [ownMessage],
    );
    notifyListeners();
    final answer = await _repository.generate(selectedModel!, prompt);
    final reply = ChatMessage(false,answer);
    state = ChatState(
      isLoading: false,
      selectedModel: selectedModel,
      models: models,
      messages: [ownMessage, reply],
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
