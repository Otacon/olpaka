import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:olpaka/core/analytics/event.dart';
import 'package:olpaka/core/analytics/screen_view.dart';
import 'package:olpaka/core/logger.dart';
import 'package:olpaka/core/state/theme/theme_domain.dart';

abstract class Analytics {
  void screenView(ScreenView screenView);

  void event(Event event);
}

class AnalyticsGoogle extends Analytics {
  @override
  void event(Event event) {
    logger.i("Event: $event");
    final analytics = FirebaseAnalytics.instance;
    switch (event) {
      case EventDownloadModel():
        analytics.logEvent(
          name: "download_model",
          parameters: {
            "model": event.modelName,
          },
        );
      case EventRemoveModel():
        analytics.logEvent(
          name: "remove_model",
          parameters: {
            "model": event.modelName,
          },
        );
      case EventSendMessage():
        analytics.logEvent(
          name: "send_message",
          parameters: {
            "model": event.modelName,
          },
        );
      case EventChangeThemeMode():
        final mode = switch (event.themeMode) {
          OlpakaThemeMode.system => "auto",
          OlpakaThemeMode.dark => "dark",
          OlpakaThemeMode.light => "light",
        };
        analytics.logEvent(
          name: "change_theme_mode",
          parameters: {
            "mode": mode,
          },
        );
      case EventChangeThemeColor():
        final color = switch (event.themeColor) {
          OlpakaThemeColor.olpaka => "olpaka",
          OlpakaThemeColor.blue => "blue",
          OlpakaThemeColor.green => "green",
          OlpakaThemeColor.orange => "orange",
          OlpakaThemeColor.red => "red",
          OlpakaThemeColor.purple => "purple",
          OlpakaThemeColor.grey => "grey",
        };
        analytics.logEvent(
          name: "change_theme_color",
          parameters: {
            "color": color,
          },
        );
      case EventGettingStartedNext():
        analytics.logEvent(
          name: "next_pressed",
          parameters: {"current_step": event.currentStep},
        );
      case EventGettingStartedPrevious():
        analytics.logEvent(
          name: "prev_pressed",
          parameters: {"current_step": event.currentStep},
        );
      case EventGettingStartedFinish():
        analytics.logEvent(
          name: "finish_pressed",
        );
      case EventAboutPressed():
        analytics.logEvent(
          name: "about_pressed",
        );
      case EventLaunchGettingStarted():
        analytics.logEvent(
          name: "launch_getting_started_pressed",
        );
    }
  }

  @override
  void screenView(ScreenView screenView) {
    logger.i("ScreenView: $event");
    final analytics = FirebaseAnalytics.instance;
    switch (screenView) {
      case ScreenViewChat():
        analytics.logScreenView(screenName: "chat");
      case ScreenViewModels():
        analytics.logScreenView(screenName: "models");
      case ScreenViewSettings():
        analytics.logScreenView(screenName: "settings");
      case ScreenViewGettingStarted():
        analytics.logScreenView(screenName: "getting_started");
    }
  }
}

class AnalyticsNoop extends Analytics {
  @override
  void event(Event event) {
    logger.i("Event: $event");
  }

  @override
  void screenView(ScreenView screenView) {
    logger.i("ScreenView: $event");
  }
}
