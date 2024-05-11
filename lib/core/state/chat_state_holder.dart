import 'package:flutter/foundation.dart';
import 'package:olpaka/core/ollama/repository.dart';

class ChatStateHolder with ChangeNotifier {
  final OllamaRepository _ollama;

  List<ChatMessageDomain> messages = List.empty(growable: false);
  bool isAssistantAnswering = false;

  ChatStateHolder(this._ollama);

  sendMessage(String text, String model) async {
    final newMessages = List<ChatMessageDomain>.from(messages, growable: true);
    newMessages.add(ChatMessageUserDomain(text));
    newMessages.add(ChatMessageAssistantDomain("...", false));
    messages = newMessages;
    notifyListeners();
    final response = await _ollama.generate(model, text);
    final SendMessageResult result;
    switch (response) {
      case GenerateResultSuccess():
        newMessages.removeLast();
        newMessages.add(ChatMessageAssistantDomain(response.answer, true));
        messages = newMessages;
        result = SendMessageResultSuccess();
      case GenerateResultError():
        newMessages.removeLast();
        messages = newMessages;
        result = SendMessageResultError();
    }
    notifyListeners();
    return result;
  }

  sendMessageStreaming(String text, String model) async {
    final newMessages = List<ChatMessageDomain>.from(messages, growable: true);
    newMessages.add(ChatMessageUserDomain(text));
    newMessages.add(ChatMessageAssistantDomain("...", false));
    messages = newMessages;
    notifyListeners();
    String message = "";
    bool isDone = false;
    _ollama.generateStream(model, text).listen((event) {
      switch(event){
        case GenerateStreamingResultChunk():
          message += event.chunk;
        case GenerateStreamingResultComplete():
          isDone = true;
          message += ".";
      }
      newMessages.removeLast();
      newMessages.add(ChatMessageAssistantDomain(message, isDone));
      notifyListeners();
    });
  }
}

sealed class SendMessageResult {}

class SendMessageResultSuccess extends SendMessageResult {}

class SendMessageResultError extends SendMessageResult {}

sealed class ChatMessageDomain {}

class ChatMessageUserDomain extends ChatMessageDomain {
  final String message;

  ChatMessageUserDomain(this.message);
}

class ChatMessageAssistantDomain extends ChatMessageDomain {
  final String message;
  final bool isFinalised;

  ChatMessageAssistantDomain(this.message, this.isFinalised);
}
