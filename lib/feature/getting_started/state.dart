class GettingStartedState {
  final int currentStep;
  final bool showPrevious;
  final bool isLastStep;
  final bool? isConnected;

  GettingStartedState([
    this.currentStep = 0,
    this.showPrevious = false,
    this.isLastStep = false,
    this.isConnected,
  ]);
}
