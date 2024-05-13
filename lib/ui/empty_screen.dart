import 'package:flutter/material.dart';
import 'package:olpaka/ui/markdown.dart';

class EmptyScreen extends StatelessWidget {
  final String header;
  final String? body;
  final VoidCallback? onCtaClicked;
  final String ctaText;

  const EmptyScreen({
    super.key,
    required this.header,
    required this.body,
    this.onCtaClicked,
    this.ctaText = "",
  });

  @override
  Widget build(BuildContext context) {
    final fBody = body;
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        child: Card(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const SizedBox(height: 8),
                Text(
                  header,
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                if (fBody != null) const SizedBox(height: 8),
                if (fBody != null)
                  Markdown(
                    fBody,
                    selectable: false,
                  ),
                if (onCtaClicked != null) const SizedBox(height: 16),
                if (onCtaClicked != null)
                  FilledButton(
                    onPressed: onCtaClicked,
                    child: Text(ctaText),
                  ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
