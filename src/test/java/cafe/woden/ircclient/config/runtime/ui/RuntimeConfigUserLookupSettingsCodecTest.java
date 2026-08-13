package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUserLookupSettingsCodec.normalizeMaxNicksPerCommand;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUserLookupSettingsCodec.normalizeMinimumOne;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUserLookupSettingsCodec.normalizeMonitorIsonPollIntervalSeconds;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUserLookupSettingsCodec.normalizePeriodicRefreshIntervalSeconds;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUserLookupSettingsCodec.normalizePeriodicRefreshNicksPerTick;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuntimeConfigUserLookupSettingsCodecTest {

  @Test
  void normalizesUserhostAndWhoisMinimums() {
    assertEquals(1, normalizeMinimumOne(0));
    assertEquals(1, normalizeMinimumOne(-5));
    assertEquals(12, normalizeMinimumOne(12));
  }

  @Test
  void normalizesUserhostBatchSizes() {
    assertEquals(1, normalizeMaxNicksPerCommand(0));
    assertEquals(5, normalizeMaxNicksPerCommand(99));
    assertEquals(3, normalizeMaxNicksPerCommand(3));
  }

  @Test
  void normalizesMonitorAndPeriodicRefreshBounds() {
    assertEquals(5, normalizeMonitorIsonPollIntervalSeconds(1));
    assertEquals(600, normalizeMonitorIsonPollIntervalSeconds(1_000));
    assertEquals(60, normalizeMonitorIsonPollIntervalSeconds(60));
    assertEquals(5, normalizePeriodicRefreshIntervalSeconds(0));
    assertEquals(90, normalizePeriodicRefreshIntervalSeconds(90));
    assertEquals(1, normalizePeriodicRefreshNicksPerTick(0));
    assertEquals(20, normalizePeriodicRefreshNicksPerTick(99));
    assertEquals(8, normalizePeriodicRefreshNicksPerTick(8));
  }
}
