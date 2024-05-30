import 'dart:async';

import 'package:olpaka/core/state/chat/chat_state_holder.dart';
import 'package:olpaka/core/state/models/model_state_holder.dart';
import 'package:olpaka/feature/chat/state.dart';
import 'package:olpaka/feature/chat_sessions/events.dart';
import 'package:olpaka/feature/chat_sessions/state.dart';
import 'package:stacked/stacked.dart';

class ChatSessionsViewModel extends BaseViewModel {
  ChatSessionsState state = ChatSessionsState(List.empty());

  final ChatStateHolder _stateHolder;
  final ModelStateHolder _modelStateHolder;

  final _events = StreamController<ChatSessionsEvent>.broadcast();
  Stream<ChatSessionsEvent> get events => _events.stream.map((val) => val);

  ChatSessionsViewModel(this._stateHolder, this._modelStateHolder);

  onCreate() async {
    _stateHolder.conversations.addListener(_onConversationsChanged);
    notifyListeners();
  }

  onNewConversation(String model) async {
    _stateHolder.startConversation(model, model);
  }

  _onConversationsChanged() {
    final conversations = _stateHolder.conversations.value
        .map((c) => ChatSession(c.id, c.model))
        .toList();
    state = ChatSessionsState(conversations);
    notifyListeners();
  }

  onCreateChatClicked() async {
    final models = _modelStateHolder.cachedModels.value.map((model) => ChatModel(model.id, model.name)).toList();
    _events.add(ChatSessionsEventShowCreateChatDialog(models));
  }
}
