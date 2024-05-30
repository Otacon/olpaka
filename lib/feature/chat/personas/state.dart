class PersonasState {
  final List<Persona> chats;

  PersonasState(this.chats);
}

class Persona {
  final String id;
  final String name;
  final String model;

  Persona(this.id, this.name, this.model);
}
