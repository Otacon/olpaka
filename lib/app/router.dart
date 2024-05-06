import 'package:go_router/go_router.dart';
import 'package:olpaka/home/view.dart';
import 'package:olpaka/onboarding/view.dart';

final router = GoRouter(
  routes: [
    GoRoute(
      name: "Olpaka",
      path: "/",
      builder: (_, __) => const OnboardingScreen(),
    ),
    GoRoute(
      name: "Home",
      path: "/home",
      builder: (_, __) => const HomeView(),
    )
  ],
);
