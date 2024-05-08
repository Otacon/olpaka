import 'package:get_it/get_it.dart';
import 'package:olpaka/feature/onboarding/view_model.dart';

registerOnboarding() {
  final l = GetIt.instance;
  l.registerFactory(() => OnboardingViewModel(l.get()));
}
