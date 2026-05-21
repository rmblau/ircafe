package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SettingsRangeSupportTest {

  @Test
  void normalizesHistoryRanges() {
    assertEquals(0, SettingsRangeSupport.normalizeChatHistoryInitialLoadLines(-1));
    assertEquals(200, SettingsRangeSupport.normalizeChatHistoryPageSize(0));
    assertEquals(100, SettingsRangeSupport.normalizeChatHistoryAutoLoadWheelDebounceMs(50));
    assertEquals(2000, SettingsRangeSupport.normalizeChatHistoryAutoLoadWheelDebounceMs(0));
    assertEquals(500, SettingsRangeSupport.normalizeChatHistoryLoadOlderChunkSize(700));
    assertEquals(0, SettingsRangeSupport.normalizeChatHistoryLoadOlderChunkDelayMs(-1));
    assertEquals(33, SettingsRangeSupport.normalizeChatHistoryLoadOlderChunkEdtBudgetMs(40));
    assertEquals(120, SettingsRangeSupport.normalizeChatHistoryRemoteRequestTimeoutSeconds(500));
    assertEquals(18, SettingsRangeSupport.normalizeChatHistoryRemoteZncPlaybackTimeoutSeconds(0));
    assertEquals(
        1440, SettingsRangeSupport.normalizeChatHistoryRemoteZncPlaybackWindowMinutes(2000));
    assertEquals(500, SettingsRangeSupport.normalizeCommandHistoryMaxSize(0));
    assertEquals(200_000, SettingsRangeSupport.normalizeChatTranscriptMaxLinesPerTarget(300_000));
    assertEquals(0, SettingsRangeSupport.normalizeLoggingRetentionDays(-1));
    assertEquals(1_000_000, SettingsRangeSupport.normalizeLoggingWriterQueueMax(2_000_000));
    assertEquals(10_000, SettingsRangeSupport.normalizeLoggingWriterBatchSize(20_000));
  }

  @Test
  void normalizesUserLookupRanges() {
    assertEquals(7, SettingsRangeSupport.normalizeUserhostMinIntervalSeconds(0));
    assertEquals(6, SettingsRangeSupport.normalizeUserhostMaxCommandsPerMinute(0));
    assertEquals(30, SettingsRangeSupport.normalizeUserhostNickCooldownMinutes(0));
    assertEquals(5, SettingsRangeSupport.normalizeUserhostMaxNicksPerCommand(12));
    assertEquals(15, SettingsRangeSupport.normalizeEnrichmentUserhostMinIntervalSeconds(0));
    assertEquals(3, SettingsRangeSupport.normalizeEnrichmentUserhostMaxCommandsPerMinute(0));
    assertEquals(60, SettingsRangeSupport.normalizeEnrichmentUserhostNickCooldownMinutes(0));
    assertEquals(45, SettingsRangeSupport.normalizeEnrichmentWhoisMinIntervalSeconds(0));
    assertEquals(120, SettingsRangeSupport.normalizeEnrichmentWhoisNickCooldownMinutes(0));
    assertEquals(300, SettingsRangeSupport.normalizeEnrichmentPeriodicRefreshIntervalSeconds(0));
    assertEquals(10, SettingsRangeSupport.normalizeEnrichmentPeriodicRefreshNicksPerTick(99));
    assertEquals(5, SettingsRangeSupport.normalizeMonitorIsonFallbackPollIntervalSeconds(1));
  }

  @Test
  void normalizesGeneralSettingsRanges() {
    assertEquals(0, SettingsRangeSupport.normalizeImageEmbedDimensionPx(-10));
    assertEquals(15, SettingsRangeSupport.normalizeNotificationRuleCooldownSeconds(-1));
    assertEquals(0, SettingsRangeSupport.normalizeNotificationRuleCooldownSeconds(0));
    assertEquals(3600, SettingsRangeSupport.normalizeNotificationRuleCooldownSeconds(5000));
    assertEquals(250, SettingsRangeSupport.normalizeMemoryUsageRefreshIntervalMs(100));
    assertEquals(1000, SettingsRangeSupport.normalizeMemoryUsageRefreshIntervalMs(0));
    assertEquals(50, SettingsRangeSupport.normalizeMemoryUsageWarningNearMaxPercent(99));
    assertEquals(5, SettingsRangeSupport.normalizeMemoryUsageWarningNearMaxPercent(0));
    assertEquals(50, SettingsRangeSupport.normalizeServerTreeUnreadBadgeScalePercent(20));
    assertEquals(100, SettingsRangeSupport.normalizeServerTreeUnreadBadgeScalePercent(0));
    assertEquals(150, SettingsRangeSupport.normalizeServerTreeUnreadBadgeScalePercent(200));
    assertEquals(0, SettingsRangeSupport.normalizeLaunchJvmMemoryMiB(-1));
    assertEquals(262_144, SettingsRangeSupport.normalizeLaunchJvmMemoryMiB(300_000));
    assertEquals(500, SettingsRangeSupport.normalizeAssertjSwingFreezeThresholdMs(100));
    assertEquals(10_000, SettingsRangeSupport.normalizeAssertjSwingWatchdogPollMs(20_000));
    assertEquals(
        120_000, SettingsRangeSupport.normalizeAssertjSwingFallbackViolationReportMs(200_000));
    assertEquals(100, SettingsRangeSupport.normalizeThemePercent(150));
    assertEquals(0, SettingsRangeSupport.normalizeThemeCornerRadius(-1));
    assertEquals(8, SettingsRangeSupport.normalizeFontSize(4));
    assertEquals(48, SettingsRangeSupport.normalizeThemeUiFontSize(96));
  }
}
