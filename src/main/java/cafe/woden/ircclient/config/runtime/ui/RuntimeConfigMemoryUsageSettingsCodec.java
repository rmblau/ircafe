package cafe.woden.ircclient.config.runtime.ui;

import java.util.Locale;
import java.util.Objects;

/** Pure normalization helpers for persisted memory usage indicator settings. */
final class RuntimeConfigMemoryUsageSettingsCodec {

  private RuntimeConfigMemoryUsageSettingsCodec() {}

  static String normalizeDisplayMode(String mode) {
    String normalized = Objects.toString(mode, "").trim().toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "short", "compact" -> "short";
      case "indicator", "gauge", "bar" -> "indicator";
      case "moon", "moon-phase", "moon-phases", "lunar" -> "moon";
      case "hidden", "off", "none", "disable", "disabled" -> "hidden";
      default -> "long";
    };
  }

  static int normalizeRefreshIntervalMs(int intervalMs) {
    int value = intervalMs;
    if (value <= 0) value = 1000;
    return clamp(value, 250, 60_000);
  }

  static int normalizeWarningNearMaxPercent(int percent) {
    return clamp(percent, 1, 50);
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
