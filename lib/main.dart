import 'package:flutter/material.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:olpaka/app/di.dart';
import 'package:olpaka/app/olpaka_app.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  registerModules();
  await _configureLocalization();
  runApp(const OlpakaApp());
}


_configureLocalization() async {
  await S.load(const Locale("en"));
}
