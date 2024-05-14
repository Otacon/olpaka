import 'package:flutter/foundation.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/core/analytics/analytics.dart';

registerAnalytics() {
  final l = GetIt.instance;
  l.registerFactory<Analytics>(() {
    if (kDebugMode) {
      return AnalyticsNoop();
    } else {
      return AnalyticsGoogle();
    }
  });
}
