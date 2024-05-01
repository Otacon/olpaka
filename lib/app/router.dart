import 'package:go_router/go_router.dart';
import 'package:olpaka/chat/view.dart';
import 'package:olpaka/onboarding/view.dart';

final router = GoRouter(
  routes: [
    GoRoute(
      name: "Olpaka",
      path: "/",
      builder: (_, __) => const OnboardingScreen(),
    ),
    GoRoute(
      name: "Chat",
      path: "/chat",
      builder: (_, __) => const ChatScreen(),
    )
  ],
);
