
import 'package:flutter/material.dart';
import 'package:markdown_widget/config/configs.dart';
import 'package:markdown_widget/widget/markdown_block.dart';

class Markdown extends StatelessWidget {
  final String text;
  final bool selectable;

  const Markdown(this.text, {this.selectable = false, super.key});

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final config =  isDark ? MarkdownConfig.darkConfig : MarkdownConfig.defaultConfig;
    return MarkdownBlock(
      config: config,
      data: text,
      selectable: selectable,
    );
  }
}