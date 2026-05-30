package cafe.woden.ircclient.config;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns memory usage indicator settings under {@code ircafe.ui}. */
class RuntimeConfigMemoryUsageStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigMemoryUsageStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigMemoryUsageStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberDisplayMode(String mode) {
    String normalized = normalizeDisplayMode(mode);
    rememberScalar("memoryUsageDisplayMode", normalized, "ui.memoryUsageDisplayMode");
  }

  synchronized int readRefreshIntervalMs(int defaultValue) {
    return readUiInt(
        "memoryUsageRefreshIntervalMs",
        defaultValue,
        RuntimeConfigMemoryUsageStore::clampRefreshIntervalMs,
        "ui.memoryUsageRefreshIntervalMs");
  }

  synchronized void rememberRefreshIntervalMs(int intervalMs) {
    int normalized = clampRefreshIntervalMs(intervalMs);
    rememberScalar("memoryUsageRefreshIntervalMs", normalized, "ui.memoryUsageRefreshIntervalMs");
  }

  synchronized void rememberWarningNearMaxPercent(int percent) {
    int normalized = Math.max(1, Math.min(50, percent));
    rememberScalar(
        "memoryUsageWarningNearMaxPercent", normalized, "ui.memoryUsageWarningNearMaxPercent");
  }

  synchronized void rememberWarningTooltipEnabled(boolean enabled) {
    rememberWarningBoolean("memoryUsageWarningTooltipEnabled", enabled);
  }

  synchronized void rememberWarningToastEnabled(boolean enabled) {
    rememberWarningBoolean("memoryUsageWarningToastEnabled", enabled);
  }

  synchronized void rememberWarningPushyEnabled(boolean enabled) {
    rememberWarningBoolean("memoryUsageWarningPushyEnabled", enabled);
  }

  synchronized void rememberWarningSoundEnabled(boolean enabled) {
    rememberWarningBoolean("memoryUsageWarningSoundEnabled", enabled);
  }

  private int readUiInt(
      String key, int defaultValue, IntUnaryOperator normalizer, String description) {
    int fallback = normalizer.applyAsInt(defaultValue);
    try {
      if (file.toString().isBlank()) return fallback;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      return RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "ui", key)
          .flatMap(RuntimeConfigMemoryUsageStore::asInt)
          .map(normalizer::applyAsInt)
          .orElse(fallback);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read {} from '{}'", description, file, e);
      return fallback;
    }
  }

  private void rememberWarningBoolean(String key, boolean enabled) {
    rememberScalar(key, enabled, "ui." + key);
  }

  private void rememberScalar(String key, Object value, String description) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = getOrCreateMapPath(doc, "ircafe", "ui");

      ui.put(key, value);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} setting to '{}'", description, file, e);
    }
  }

  private static String normalizeDisplayMode(String mode) {
    String normalized = Objects.toString(mode, "").trim().toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "short", "compact" -> "short";
      case "indicator", "gauge", "bar" -> "indicator";
      case "moon", "moon-phase", "moon-phases", "lunar" -> "moon";
      case "hidden", "off", "none", "disable", "disabled" -> "hidden";
      default -> "long";
    };
  }

  private static int clampRefreshIntervalMs(int intervalMs) {
    int value = intervalMs;
    if (value <= 0) value = 1000;
    if (value < 250) value = 250;
    if (value > 60_000) value = 60_000;
    return value;
  }

  private static Map<String, Object> getOrCreateMapPath(Map<String, Object> root, String... path) {
    Map<String, Object> current = root;
    for (String segment : path) {
      current = getOrCreateMap(current, segment);
    }
    return current;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }

  private static Optional<Integer> asInt(Object value) {
    if (value instanceof Number n) return Optional.of(n.intValue());
    if (value instanceof String s) {
      String t = s.trim();
      if (t.isEmpty()) return Optional.empty();
      try {
        return Optional.of(Integer.parseInt(t));
      } catch (Exception ignored) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }
}
