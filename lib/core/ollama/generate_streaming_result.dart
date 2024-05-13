
sealed class GenerateStreamingResult {}

class GenerateStreamingResultSuccess extends GenerateStreamingResult {
  final Stream<GenerateChunk> chunkStream;

  GenerateStreamingResultSuccess(this.chunkStream);
}

class GenerateStreamingResultConnectionError extends GenerateStreamingResult {}

class GenerateStreamingResultError extends GenerateStreamingResult {}

class GenerateChunk {
  final String message;
  final List<int>? context;
  final bool done;

  GenerateChunk(this.message, this.context, this.done);
}