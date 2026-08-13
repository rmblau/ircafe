package cafe.woden.ircclient.config.runtime.ui;

/** Pure normalization helpers for persisted chat history and transcript settings. */
final class RuntimeConfigChatHistorySettingsCodec {

  private RuntimeConfigChatHistorySettingsCodec() {}

  static int normalizeInitialLoadLines(int lines) {
    return Math.max(0, lines);
  }

  static int normalizePageSize(int pageSize) {
    return Math.max(1, pageSize);
  }

  static int normalizeAutoLoadWheelDebounceMs(int debounceMs) {
    return clamp(debounceMs, 100, 30_000);
  }

  static int normalizeLoadOlderChunkSize(int chunkSize) {
    return clamp(chunkSize, 1, 500);
  }

  static int normalizeLoadOlderChunkDelayMs(int chunkDelayMs) {
    return clamp(chunkDelayMs, 0, 1_000);
  }

  static int normalizeLoadOlderChunkEdtBudgetMs(int chunkEdtBudgetMs) {
    return clamp(chunkEdtBudgetMs, 1, 33);
  }

  static int normalizeRemoteRequestTimeoutSeconds(int seconds) {
    return clamp(seconds, 1, 120);
  }

  static int normalizeRemoteZncPlaybackTimeoutSeconds(int seconds) {
    return clamp(seconds, 1, 300);
  }

  static int normalizeRemoteZncPlaybackWindowMinutes(int minutes) {
    return clamp(minutes, 1, 1440);
  }

  static int normalizeCommandHistoryMaxSize(int maxSize) {
    if (maxSize <= 0) return 500;
    return Math.min(maxSize, 500);
  }

  static int normalizeTranscriptMaxLinesPerTarget(int maxLines) {
    return clamp(maxLines, 0, 200_000);
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
