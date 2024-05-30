import 'package:olpaka/core/state/chat/chat_message_domain.dart';
import 'package:uuid/uuid.dart';

class ChatPersonaDomain {
  final String id;
  final String name;
  final String model;
  final List<ChatMessageDomain> messages;
  final List<int>? context;

  ChatPersonaDomain({
    required this.id,
    required this.name,
    required this.messages,
    required this.model,
    required this.context,
  });

  ChatPersonaDomain.create(String name, String model)
      : this(
          id: const Uuid().v4().toString(),
          name: name,
          messages: List.empty(),
          model: model,
          context: null,
        );
}
