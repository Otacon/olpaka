import 'package:flutter/foundation.dart';
import 'package:olpaka/core/ollama/repository.dart';

class ChatStateHolder with ChangeNotifier {
  final OllamaRepository _ollama;

  List<ChatMessageDomain> messages = List.empty(growable: false);
  bool isAssistantAnswering = false;

  ChatStateHolder(this._ollama);

  Future<SendMessageResult> sendMessage(String text, String model) async {
    final newMessages = List<ChatMessageDomain>.from(messages, growable: true);
    newMessages.add(ChatMessageUserDomain(text));
    newMessages.add(ChatMessageAssistantDomain("...", false));
    messages = newMessages;
    notifyListeners();

    final response = await _ollama.generate(model, text);
    final Stream<GenerateChunk> stream;
    switch(response){
      case GenerateStreamingResultSuccess():
        stream = response.chunkStream;
      case GenerateStreamingResultConnectionError():
      case GenerateStreamingResultError():
        newMessages.removeLast();
        messages = newMessages;
        notifyListeners();
        return SendMessageResultError();
    }

    String message = "";
    await for (final chunkResult in stream) {
      message += chunkResult.message;
      newMessages.removeLast();
      newMessages.add(ChatMessageAssistantDomain(message, chunkResult.done));
      messages = newMessages;
      notifyListeners();
    }
    return SendMessageResultSuccess();
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
