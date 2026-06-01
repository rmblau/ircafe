package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.function.IntUnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns memory usage indicator settings under {@code ircafe.ui}. */
public class RuntimeConfigMemoryUsageStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigMemoryUsageStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigMemoryUsageStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized void rememberDisplayMode(String mode) {
    String normalized = normalizeDisplayMode(mode);
    rememberScalar("memoryUsageDisplayMode", normalized, "ui.memoryUsageDisplayMode");
  }

  public synchronized int readRefreshIntervalMs(int defaultValue) {
    return readUiInt(
        "memoryUsageRefreshIntervalMs",
        defaultValue,
        RuntimeConfigMemoryUsageStore::clampRefreshIntervalMs,
        "ui.memoryUsageRefreshIntervalMs");
  }

  public synchronized void rememberRefreshIntervalMs(int intervalMs) {
    int normalized = clampRefreshIntervalMs(intervalMs);
    rememberScalar("memoryUsageRefreshIntervalMs", normalized, "ui.memoryUsageRefreshIntervalMs");
  }

  public synchronized void rememberWarningNearMaxPercent(int percent) {
    int normalized = Math.max(1, Math.min(50, percent));
    rememberScalar(
        "memoryUsageWarningNearMaxPercent", normalized, "ui.memoryUsageWarningNearMaxPercent");
  }

  public synchronized void rememberWarningTooltipEnabled(boolean enabled) {
    rememberWarningBoolean("memoryUsageWarningTooltipEnabled", enabled);
  }

  public synchronized void rememberWarningToastEnabled(boolean enabled) {
    rememberWarningBoolean("memoryUsageWarningToastEnabled", enabled);
  }

  public synchronized void rememberWarningPushyEnabled(boolean enabled) {
    rememberWarningBoolean("memoryUsageWarningPushyEnabled", enabled);
  }

  public synchronized void rememberWarningSoundEnabled(boolean enabled) {
    rememberWarningBoolean("memoryUsageWarningSoundEnabled", enabled);
  }

  private int readUiInt(
      String key, int defaultValue, IntUnaryOperator normalizer, String description) {
    int fallback = normalizer.applyAsInt(defaultValue);
    return uiSection
        .readValue(description, key)
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(normalizer::applyAsInt)
        .orElse(fallback);
  }

  private void rememberWarningBoolean(String key, boolean enabled) {
    rememberScalar(key, enabled, "ui." + key);
  }

  private void rememberScalar(String key, Object value, String description) {
    uiSection.putValue(description, value, key);
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
}
