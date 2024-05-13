import 'package:flutter/material.dart';
import 'package:olpaka/generated/l10n.dart';

class Loading extends StatelessWidget {
  final String? text;

  const Loading({
    super.key,
    this.text,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const CircularProgressIndicator(),
          const SizedBox(height: 16),
          Text(text ?? S.current.loading),
        ],
      ),
    );
  }
}
