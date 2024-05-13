class Model {
  final String name;
  final String model;
  final String? modifiedAt;
  final int? size;
  final String? digest;
  final String? parentModel;
  final String? format;
  final String? family;
  final List<String>? families;
  final String? parameterSize;
  final String? quantizationLevel;

  Model({
    required this.name,
    required this.model,
    this.modifiedAt,
    this.size,
    this.digest,
    this.parentModel,
    this.format,
    this.family,
    this.families,
    this.parameterSize,
    this.quantizationLevel,
  });

  @override
  String toString() {
    return 'Model{name: $name, model: $model, modifiedAt: $modifiedAt, size: $size, digest: $digest, parentModel: $parentModel, format: $format, family: $family, families: $families, parameterSize: $parameterSize, quantizationLevel: $quantizationLevel}';
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is Model &&
          runtimeType == other.runtimeType &&
          name == other.name &&
          model == other.model &&
          modifiedAt == other.modifiedAt &&
          size == other.size &&
          digest == other.digest &&
          parentModel == other.parentModel &&
          format == other.format &&
          family == other.family &&
          families == other.families &&
          parameterSize == other.parameterSize &&
          quantizationLevel == other.quantizationLevel;

  @override
  int get hashCode =>
      name.hashCode ^
      model.hashCode ^
      modifiedAt.hashCode ^
      size.hashCode ^
      digest.hashCode ^
      parentModel.hashCode ^
      format.hashCode ^
      family.hashCode ^
      families.hashCode ^
      parameterSize.hashCode ^
      quantizationLevel.hashCode;
}
