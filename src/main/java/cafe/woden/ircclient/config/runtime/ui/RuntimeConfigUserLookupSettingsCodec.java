package cafe.woden.ircclient.config.runtime.ui;

/** Pure normalization helpers for persisted user lookup and enrichment settings. */
final class RuntimeConfigUserLookupSettingsCodec {

  private RuntimeConfigUserLookupSettingsCodec() {}

  static int normalizeMinimumOne(int value) {
    return Math.max(1, value);
  }

  static int normalizeMaxNicksPerCommand(int maxNicks) {
    return clamp(maxNicks, 1, 5);
  }

  static int normalizeMonitorIsonPollIntervalSeconds(int seconds) {
    return clamp(seconds, 5, 600);
  }

  static int normalizePeriodicRefreshIntervalSeconds(int seconds) {
    return Math.max(5, seconds);
  }

  static int normalizePeriodicRefreshNicksPerTick(int nicksPerTick) {
    return clamp(nicksPerTick, 1, 20);
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
