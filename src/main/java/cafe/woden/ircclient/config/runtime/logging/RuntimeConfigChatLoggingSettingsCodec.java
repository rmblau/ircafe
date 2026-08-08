package cafe.woden.ircclient.config.runtime.logging;

import java.util.Objects;

/** Pure normalization helpers for persisted chat logging settings. */
final class RuntimeConfigChatLoggingSettingsCodec {

  static final String DEFAULT_DB_FILE_BASE_NAME = "ircafe-chatlog";

  private RuntimeConfigChatLoggingSettingsCodec() {}

  static String normalizeDbFileBaseName(String fileBaseName) {
    String base = Objects.toString(fileBaseName, "").trim();
    return base.isEmpty() ? DEFAULT_DB_FILE_BASE_NAME : base;
  }

  static int normalizeRetentionDays(int retentionDays) {
    return Math.max(0, retentionDays);
  }

  static int normalizeWriterQueueMax(int writerQueueMax) {
    return clamp(writerQueueMax, 100, 1_000_000);
  }

  static int normalizeWriterBatchSize(int writerBatchSize) {
    return clamp(writerBatchSize, 1, 10_000);
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
