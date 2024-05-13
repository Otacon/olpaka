import 'package:go_router/go_router.dart';
import 'package:olpaka/feature/home/view.dart';

final router = GoRouter(
  routes: [
    GoRoute(
      name: "Olpaka",
      path: "/",
      builder: (_, __) => const HomeView(),
    )
  ],
);
