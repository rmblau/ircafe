package cafe.woden.ircclient.ui.settings;

public final class SettingsRangeSupport {
  private SettingsRangeSupport() {}

  public static int normalizeImageEmbedDimensionPx(int value) {
    return Math.max(0, value);
  }

  public static int normalizeChatHistoryInitialLoadLines(int value) {
    return Math.max(0, value);
  }

  public static int normalizeChatHistoryPageSize(int value) {
    return positiveOrDefault(value, 200);
  }

  public static int normalizeChatHistoryAutoLoadWheelDebounceMs(int value) {
    return clampedPositiveOrDefault(value, 2000, 100, 30_000);
  }

  public static int normalizeChatHistoryLoadOlderChunkSize(int value) {
    return clampedPositiveOrDefault(value, 20, 1, 500);
  }

  public static int normalizeChatHistoryLoadOlderChunkDelayMs(int value) {
    return SettingsValueSupport.clampInt(value, 0, 1_000);
  }

  public static int normalizeChatHistoryLoadOlderChunkEdtBudgetMs(int value) {
    return clampedPositiveOrDefault(value, 6, 1, 33);
  }

  public static int normalizeChatHistoryRemoteRequestTimeoutSeconds(int value) {
    return clampedPositiveOrDefault(value, 6, 1, 120);
  }

  public static int normalizeChatHistoryRemoteZncPlaybackTimeoutSeconds(int value) {
    return clampedPositiveOrDefault(value, 18, 1, 300);
  }

  public static int normalizeChatHistoryRemoteZncPlaybackWindowMinutes(int value) {
    return clampedPositiveOrDefault(value, 360, 1, 1440);
  }

  public static int normalizeCommandHistoryMaxSize(int value) {
    return clampedPositiveOrDefault(value, 500, 1, 500);
  }

  public static int normalizeChatTranscriptMaxLinesPerTarget(int value) {
    return SettingsValueSupport.clampInt(value, 0, 200_000);
  }

  public static int normalizeLoggingRetentionDays(int value) {
    return Math.max(0, value);
  }

  public static int normalizeLoggingWriterQueueMax(int value) {
    return SettingsValueSupport.clampInt(value, 100, 1_000_000);
  }

  public static int normalizeLoggingWriterBatchSize(int value) {
    return SettingsValueSupport.clampInt(value, 1, 10_000);
  }

  public static int normalizeUserhostMinIntervalSeconds(int value) {
    return positiveOrDefault(value, 7);
  }

  public static int normalizeUserhostMaxCommandsPerMinute(int value) {
    return positiveOrDefault(value, 6);
  }

  public static int normalizeUserhostNickCooldownMinutes(int value) {
    return positiveOrDefault(value, 30);
  }

  public static int normalizeUserhostMaxNicksPerCommand(int value) {
    return clampedPositiveOrDefault(value, 5, 1, 5);
  }

  public static int normalizeEnrichmentUserhostMinIntervalSeconds(int value) {
    return positiveOrDefault(value, 15);
  }

  public static int normalizeEnrichmentUserhostMaxCommandsPerMinute(int value) {
    return positiveOrDefault(value, 3);
  }

  public static int normalizeEnrichmentUserhostNickCooldownMinutes(int value) {
    return positiveOrDefault(value, 60);
  }

  public static int normalizeEnrichmentWhoisMinIntervalSeconds(int value) {
    return positiveOrDefault(value, 45);
  }

  public static int normalizeEnrichmentWhoisNickCooldownMinutes(int value) {
    return positiveOrDefault(value, 120);
  }

  public static int normalizeEnrichmentPeriodicRefreshIntervalSeconds(int value) {
    return positiveOrDefault(value, 300);
  }

  public static int normalizeEnrichmentPeriodicRefreshNicksPerTick(int value) {
    return clampedPositiveOrDefault(value, 2, 1, 10);
  }

  public static int normalizeMonitorIsonFallbackPollIntervalSeconds(int value) {
    return clampedPositiveOrDefault(value, 30, 5, 600);
  }

  public static int normalizeNotificationRuleCooldownSeconds(int value) {
    return SettingsValueSupport.clampInt(value < 0 ? 15 : value, 0, 3600);
  }

  public static int normalizeMemoryUsageRefreshIntervalMs(int value) {
    return clampedPositiveOrDefault(value, 1000, 250, 60_000);
  }

  public static int normalizeMemoryUsageWarningNearMaxPercent(int value) {
    return clampedPositiveOrDefault(value, 5, 1, 50);
  }

  public static int normalizeServerTreeUnreadBadgeScalePercent(int value) {
    return clampedPositiveOrDefault(value, 100, 50, 150);
  }

  public static int normalizeLaunchJvmMemoryMiB(int value) {
    return SettingsValueSupport.clampInt(value, 0, 262_144);
  }

  public static int normalizeAssertjSwingFreezeThresholdMs(int value) {
    return SettingsValueSupport.clampInt(value, 500, 120_000);
  }

  public static int normalizeAssertjSwingWatchdogPollMs(int value) {
    return SettingsValueSupport.clampInt(value, 100, 10_000);
  }

  public static int normalizeAssertjSwingFallbackViolationReportMs(int value) {
    return SettingsValueSupport.clampInt(value, 250, 120_000);
  }

  public static int normalizeThemePercent(int value) {
    return SettingsValueSupport.clampInt(value, 0, 100);
  }

  public static int normalizeThemeCornerRadius(int value) {
    return SettingsValueSupport.clampInt(value, 0, 20);
  }

  public static int normalizeFontSize(int value) {
    return SettingsValueSupport.clampInt(value, 8, 48);
  }

  public static int normalizeThemeUiFontSize(int value) {
    return normalizeFontSize(value);
  }

  private static int positiveOrDefault(int value, int defaultValue) {
    return value <= 0 ? defaultValue : value;
  }

  private static int clampedPositiveOrDefault(int value, int defaultValue, int min, int max) {
    return SettingsValueSupport.clampInt(positiveOrDefault(value, defaultValue), min, max);
  }
}
