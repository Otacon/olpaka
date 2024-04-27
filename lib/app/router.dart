import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:olpaka/chat/view.dart';

final router = GoRouter(
  routes: [
    GoRoute(
      name: "Olpaka",
      path: "/",
      builder: (_, __) => const ChatScreen(),
    ),
  ],
);
