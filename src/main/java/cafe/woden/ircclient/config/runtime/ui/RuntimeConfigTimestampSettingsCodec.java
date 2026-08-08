package cafe.woden.ircclient.config.runtime.ui;

/** Pure normalization helpers for persisted timestamp settings. */
final class RuntimeConfigTimestampSettingsCodec {

  static final String DEFAULT_FORMAT = "HH:mm:ss";

  private RuntimeConfigTimestampSettingsCodec() {}

  static String normalizeFormat(String format) {
    if (format == null || format.isBlank()) {
      return DEFAULT_FORMAT;
    }
    return format.trim();
  }
}
