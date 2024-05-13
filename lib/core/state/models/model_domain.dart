sealed class ModelDomain {
  final String id;
  final String name;

  ModelDomain({
    required this.id,
    required this.name,
  });
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
}

class ModelDomainError extends ModelDomain {
  final String? error;

  ModelDomainError({
    required super.id,
    required super.name,
    required this.error,
  });
}
