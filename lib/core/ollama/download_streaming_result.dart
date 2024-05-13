sealed class DownloadModelStreamingResult {}

class DownloadModelStreamingResultSuccess extends DownloadModelStreamingResult {
  final Stream<DownloadChunk> chunkStream;

  DownloadModelStreamingResultSuccess(this.chunkStream);
}

class DownloadModelStreamingResultNotFound
    extends DownloadModelStreamingResult {}

class DownloadModelStreamingResultConnectionError
    extends DownloadModelStreamingResult {}

sealed class DownloadChunk {}

class DownloadChunkError extends DownloadChunk {
  final String message;

  DownloadChunkError({
    required this.message,
  });
}

class DownloadChunkProgress extends DownloadChunk {
  final String status;
  final String? digest;
  final int? total;
  final int? completed;

  DownloadChunkProgress({
    required this.status,
    required this.digest,
    required this.total,
    required this.completed,
  });
}
