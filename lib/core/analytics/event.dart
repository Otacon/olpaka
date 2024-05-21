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

class EventChangeThemeMode extends Event{
  OlpakaThemeMode themeMode;

  EventChangeThemeMode(this.themeMode);

  @override
  String toString() {
    return 'EventChangeThemeMode{themeMode: $themeMode}';
  }
}

class EventChangeThemeColor extends Event{
  OlpakaThemeColor themeColor;

  EventChangeThemeColor(this.themeColor);

  @override
  String toString() {
    return 'EventChangeThemeColor{themeColor: $themeColor}';
  }
}
