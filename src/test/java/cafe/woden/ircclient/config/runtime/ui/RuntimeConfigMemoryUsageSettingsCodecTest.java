package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigMemoryUsageSettingsCodec.normalizeDisplayMode;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigMemoryUsageSettingsCodec.normalizeRefreshIntervalMs;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigMemoryUsageSettingsCodec.normalizeWarningNearMaxPercent;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuntimeConfigMemoryUsageSettingsCodecTest {

  @Test
  void normalizeDisplayModeAcceptsAliasesAndDefaultsUnknownValuesToLong() {
    assertEquals("short", normalizeDisplayMode("compact"));
    assertEquals("indicator", normalizeDisplayMode("gauge"));
    assertEquals("moon", normalizeDisplayMode("moon-phase"));
    assertEquals("hidden", normalizeDisplayMode("disable"));
    assertEquals("long", normalizeDisplayMode("unknown"));
    assertEquals("long", normalizeDisplayMode(null));
  }

  @Test
  void normalizeRefreshIntervalUsesDefaultForNonPositiveValuesAndClampsRange() {
    assertEquals(1000, normalizeRefreshIntervalMs(0));
    assertEquals(250, normalizeRefreshIntervalMs(50));
    assertEquals(1800, normalizeRefreshIntervalMs(1800));
    assertEquals(60_000, normalizeRefreshIntervalMs(120_000));
  }

  @Test
  void normalizeWarningNearMaxPercentClampsPersistedRange() {
    assertEquals(1, normalizeWarningNearMaxPercent(0));
    assertEquals(12, normalizeWarningNearMaxPercent(12));
    assertEquals(50, normalizeWarningNearMaxPercent(99));
  }
}
