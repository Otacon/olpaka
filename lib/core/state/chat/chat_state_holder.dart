import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:olpaka/core/ollama/repository.dart';
import 'package:olpaka/core/state/chat/chat_message_domain.dart';
import 'package:olpaka/core/state/chat/send_message_result.dart';

class ChatStateHolder {
  final OllamaRepository _ollama;

  final messages = ValueNotifier<List<ChatMessageDomain>>(List.empty());

  ChatStateHolder(this._ollama);

  Future<SendMessageResult> sendMessage(String text, String model) async {
    _addMessage(ChatMessageUserDomain(text));
    final responseIndex = _addMessage(ChatMessageAssistantDomain("...", false));

    final response = await _ollama.generate(model, text);
    final Stream<GenerateChunk> stream;
    switch (response) {
      case GenerateStreamingResultSuccess():
        stream = response.chunkStream;
      case GenerateStreamingResultConnectionError():
      case GenerateStreamingResultError():
        _removeMessage(responseIndex);
        return SendMessageResultError();
    }

    String message = "";
    await for (final chunkResult in stream) {
      message += chunkResult.message;
      _updateMessage(responseIndex, message, chunkResult.done);
    }
    return SendMessageResultSuccess();
  }

  int _addMessage(ChatMessageDomain message) {
    final currentMessages = messages.value.toList(growable: true);
    currentMessages.add(message);
    messages.value = currentMessages;
    return currentMessages.length - 1;
  }

  _removeMessage(int index) {
    final currentMessages = messages.value.toList(growable: true);
    currentMessages.removeAt(index);
    messages.value = currentMessages;
  }

  _updateMessage(int index, String text, bool isFinalised) {
    final currentMessages = messages.value.toList(growable: true);
    final messageToUpdate = currentMessages.elementAt(index);
    final ChatMessageDomain updatedMessage;
    switch (messageToUpdate) {
      case ChatMessageUserDomain():
        updatedMessage = ChatMessageUserDomain(text);
      case ChatMessageAssistantDomain():
        updatedMessage = ChatMessageAssistantDomain(text, isFinalised);
    }
    currentMessages.removeAt(index);
    currentMessages.insert(index, updatedMessage);
    messages.value = currentMessages;
  }
}