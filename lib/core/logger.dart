import 'package:logger/logger.dart';

final logger = Logger(
  filter: null,
  printer: PrettyPrinter(
    methodCount: 8,
    lineLength: 160,
    printTime: true,
    printEmojis: false,
  ),
  output: null,
);
