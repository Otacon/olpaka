import 'package:olpaka/core/state/chat/chat_message_domain.dart';
import 'package:uuid/uuid.dart';

class ChatConversationDomain {
  final String id;
  final String model;
  final List<ChatMessageDomain> messages;
  final List<int>? context;

  ChatConversationDomain({
    required this.id,
    required this.messages,
    required this.model,
    required this.context,
  });

  ChatConversationDomain.create(String model)
      : this(
          id: const Uuid().v4().toString(),
          messages: List.empty(),
          model: model,
          context: null,
        );
}
