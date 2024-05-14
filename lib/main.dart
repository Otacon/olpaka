import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:olpaka/app/olpaka_app.dart';
import 'package:olpaka/core/di.dart';
import 'package:olpaka/generated/l10n.dart';
import 'firebase_options.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await _configureLocalization();
  registerModules();
  await _configureFirebase();
  runApp(const OlpakaApp());
}

_configureLocalization() async {
  await S.load(const Locale("en"));
}

_configureFirebase() async {
  if (kDebugMode) {
    return;
  }
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
}
