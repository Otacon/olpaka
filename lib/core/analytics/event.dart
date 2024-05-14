import 'package:olpaka/core/state/theme/theme_domain.dart';

sealed class Event {}

class EventDownloadModel extends Event {
  String modelName;

  EventDownloadModel(this.modelName);
}

class EventRemoveModel extends Event {
  String modelName;

  EventRemoveModel(this.modelName);
}

class EventSendMessage extends Event {
  String modelName;

  EventSendMessage(this.modelName);
}

class EventChangeThemeMode extends Event{
  OlpakaThemeMode themeMode;

  EventChangeThemeMode(this.themeMode);
}

class EventChangeThemeColor extends Event{
  OlpakaThemeColor themeColor;

  EventChangeThemeColor(this.themeColor);
}
