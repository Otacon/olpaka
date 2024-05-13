import 'dart:math';

import 'package:intl/intl.dart';

extension SizeFormatter on num {
  String readableFileSize() {
    if (this <= 0) return "0";

    const base = 1000;
    final units = ["B", "kB", "MB", "GB", "TB"];

    int digitGroups = (log(this) / log(base)).floor();
    return "${NumberFormat("#,##0.#").format(this / pow(base, digitGroups))} ${units[digitGroups]}";
  }
}
