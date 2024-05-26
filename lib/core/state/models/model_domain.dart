sealed class ModelDomain {
  final String id;
  final String name;

  ModelDomain({
    required this.id,
    required this.name,
  });

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ModelDomain &&
          runtimeType == other.runtimeType &&
          id == other.id &&
          name == other.name;

  @override
  int get hashCode => id.hashCode ^ name.hashCode;
}

class ModelDomainDownloading extends ModelDomain {
  final String? status;
  final double? progress;

  ModelDomainDownloading({
    required super.id,
    required super.name,
    this.status,
    this.progress,
  });

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      super == other &&
          other is ModelDomainDownloading &&
          runtimeType == other.runtimeType &&
          status == other.status &&
          progress == other.progress;

  @override
  int get hashCode => super.hashCode ^ status.hashCode ^ progress.hashCode;
}

class ModelDomainAvailable extends ModelDomain {
  final String friendlyName;
  final String? params;
  final int? size;
  final String? quantization;

  ModelDomainAvailable({
    required super.id,
    required super.name,
    required this.friendlyName,
    this.params,
    this.size,
    this.quantization,
  });

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      super == other &&
          other is ModelDomainAvailable &&
          runtimeType == other.runtimeType &&
          friendlyName == other.friendlyName &&
          params == other.params &&
          size == other.size &&
          quantization == other.quantization;

  @override
  int get hashCode =>
      super.hashCode ^
      friendlyName.hashCode ^
      params.hashCode ^
      size.hashCode ^
      quantization.hashCode;
}

class ModelDomainError extends ModelDomain {
  final String? error;

  ModelDomainError({
    required super.id,
    required super.name,
    required this.error,
  });

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      super == other &&
          other is ModelDomainError &&
          runtimeType == other.runtimeType &&
          error == other.error;

  @override
  int get hashCode => super.hashCode ^ error.hashCode;
}
