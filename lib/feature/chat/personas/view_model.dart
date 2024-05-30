import 'dart:async';

import 'package:olpaka/core/state/chat/chat_state_holder.dart';
import 'package:olpaka/core/state/models/model_state_holder.dart';
import 'package:olpaka/feature/chat/conversation/state.dart';
import 'package:olpaka/feature/chat/personas/state.dart';
import 'package:olpaka/feature/chat/personas/events.dart';
import 'package:stacked/stacked.dart';

class PersonasViewModel extends BaseViewModel {
  PersonasState state = PersonasState(List.empty());

  final ChatStateHolder _stateHolder;
  final ModelStateHolder _modelStateHolder;

  final _events = StreamController<PersonasEvent>.broadcast();
  Stream<PersonasEvent> get events => _events.stream.map((val) => val);

  PersonasViewModel(this._stateHolder, this._modelStateHolder);

  onCreate() async {
    _stateHolder.personas.addListener(_onPersonasChanged);
    notifyListeners();
  }

  onNewConversation(String model) async {
    _stateHolder.startConversation(model, model);
  }

  _onPersonasChanged() {
    final conversations = _stateHolder.personas.value
        .map((c) => Persona(c.id,c.name, c.model))
        .toList();
    state = PersonasState(conversations);
    notifyListeners();
  }

  onCreateChatClicked() async {
    final models = _modelStateHolder.cachedModels.value.map((model) => ChatModel(model.id, model.name)).toList();
    _events.add(PersonasEventShowCreateChatDialog(models));
  }
}
