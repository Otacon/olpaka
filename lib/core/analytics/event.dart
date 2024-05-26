import 'package:olpaka/core/state/theme/theme_domain.dart';

sealed class Event {}

class EventDownloadModel extends Event {
  String modelName;

  EventDownloadModel(this.modelName);

  @override
  String toString() {
    return 'EventDownloadModel{modelName: $modelName}';
  }
}

class EventRemoveModel extends Event {
  String modelName;

  EventRemoveModel(this.modelName);

  @override
  String toString() {
    return 'EventRemoveModel{modelName: $modelName}';
  }
}

class EventSendMessage extends Event {
  String modelName;

  EventSendMessage(this.modelName);

  @override
  String toString() {
    return 'EventSendMessage{modelName: $modelName}';
  }
}

class EventChangeThemeMode extends Event {
  OlpakaThemeMode themeMode;

  EventChangeThemeMode(this.themeMode);

  @override
  String toString() {
    return 'EventChangeThemeMode{themeMode: $themeMode}';
  }
}

class EventChangeThemeColor extends Event {
  OlpakaThemeColor themeColor;

  EventChangeThemeColor(this.themeColor);

  @override
  String toString() {
    return 'EventChangeThemeColor{themeColor: $themeColor}';
  }
}

class EventGettingStartedNext extends Event {
  final int currentStep;

  EventGettingStartedNext(this.currentStep);

  @override
  String toString() {
    return 'EventGettingStartedNext{currentStep: $currentStep}';
  }
}

class EventGettingStartedPrevious extends Event {
  final int currentStep;

  EventGettingStartedPrevious(this.currentStep);

  @override
  String toString() {
    return 'EventGettingStartedPrevious{currentStep: $currentStep}';
  }
}

class EventGettingStartedFinish extends Event {
  @override
  String toString() {
    return 'EventGettingStartedFinish{}';
  }
}

class EventCheckConnectionPressed extends Event {
  final bool success;

  EventCheckConnectionPressed(this.success);

  @override
  String toString() {
    return 'EventCheckConnectionPressed{success: $success}';
  }
}

class EventCorsLinkPressed extends Event {
  @override
  String toString() {
    return 'EventCorsLinkClicked{}';
  }
}

class EventDownloadOllamaPressed extends Event {
  @override
  String toString() {
    return 'EventDownloadOllamaPressed{}';
  }
}

class EventLaunchGettingStarted extends Event {
  @override
  String toString() {
    return 'EventLaunchGettingStarted{}';
  }
}

class EventAboutPressed extends Event {
  @override
  String toString() {
    return 'EventAboutPressed{}';
  }
}
