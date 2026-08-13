package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.util.List;
import java.util.Objects;

/** Pure normalization helpers for persisted application diagnostics settings. */
final class RuntimeConfigAppDiagnosticsSettingsCodec {

  private RuntimeConfigAppDiagnosticsSettingsCodec() {}

  static int normalizeAssertjFreezeThresholdMs(int value) {
    return clamp(value, 500, 120_000);
  }

  static int normalizeAssertjWatchdogPollMs(int value) {
    return clamp(value, 100, 10_000);
  }

  static int normalizeAssertjFallbackViolationReportMs(int value) {
    return clamp(value, 250, 120_000);
  }

  static String normalizeString(Object value) {
    return Objects.toString(value, "").trim();
  }

  static String normalizeJavaCommandFallback(String defaultValue) {
    String fallback = normalizeString(defaultValue);
    return fallback.isEmpty() ? "java" : fallback;
  }

  static List<String> normalizeArgs(Object args) {
    return RuntimeConfigYamlSupport.sanitizeStringList(args);
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
