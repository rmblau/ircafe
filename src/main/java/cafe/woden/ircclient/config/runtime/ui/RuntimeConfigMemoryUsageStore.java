package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
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
    String normalized = RuntimeConfigMemoryUsageSettingsCodec.normalizeDisplayMode(mode);
    rememberScalar("memoryUsageDisplayMode", normalized, "ui.memoryUsageDisplayMode");
  }

  public synchronized int readRefreshIntervalMs(int defaultValue) {
    return readUiInt(
        "memoryUsageRefreshIntervalMs",
        defaultValue,
        RuntimeConfigMemoryUsageSettingsCodec::normalizeRefreshIntervalMs,
        "ui.memoryUsageRefreshIntervalMs");
  }

  public synchronized void rememberRefreshIntervalMs(int intervalMs) {
    int normalized = RuntimeConfigMemoryUsageSettingsCodec.normalizeRefreshIntervalMs(intervalMs);
    rememberScalar("memoryUsageRefreshIntervalMs", normalized, "ui.memoryUsageRefreshIntervalMs");
  }

  public synchronized void rememberWarningNearMaxPercent(int percent) {
    int normalized = RuntimeConfigMemoryUsageSettingsCodec.normalizeWarningNearMaxPercent(percent);
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
}
