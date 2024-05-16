import 'package:flutter/foundation.dart';
import 'package:logger/logger.dart';

late Logger logger;

initLogger() {
  if (kDebugMode) {
    logger = Logger(
      filter: null,
      printer: PrettyPrinter(
        methodCount: 8,
        lineLength: 160,
        printTime: true,
        printEmojis: false,
      ),
      output: null,
    );
  } else {
    logger = Logger(level: Level.off);
  }
}
