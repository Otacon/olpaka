import 'package:logger/logger.dart';

final logger = Logger(
  filter: null,
  printer: PrettyPrinter(),
  output: ConsoleOutput(),
  level: Level.all
);
