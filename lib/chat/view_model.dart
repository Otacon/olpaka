
import 'package:flutter/foundation.dart';
import 'package:olpaka/ollama/repository.dart';

class ChatViewModel with ChangeNotifier {
  final OllamaRepository _repository;

  ChatViewModel(this._repository);

  onCreate() async {
    final models = await _repository.listModels();
    final modelName = models.first.name;
    const prompt = "Why is the sky blue?";
    print("Generating answer with $modelName: $prompt\n");
    final response = await _repository.generate(models.first.name, prompt);
    print(response);
  }
}