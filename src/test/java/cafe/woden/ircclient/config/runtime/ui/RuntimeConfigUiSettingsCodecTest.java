package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUiSettingsCodec.clampCornerRadius;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUiSettingsCodec.clampPercent;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUiSettingsCodec.clampUiFontSize;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUiSettingsCodec.normalizeDensity;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUiSettingsCodec.normalizeString;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUiSettingsCodec.parseLastSelectedTarget;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUiSettingsCodec.serializeLastSelectedTarget;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.config.api.SelectedTargetRuntimeConfigPort.LastSelectedTarget;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RuntimeConfigUiSettingsCodecTest {

  @Test
  void normalizeDensityKeepsKnownTokensAndFallsBackUnknownTokensToAuto() {
    assertEquals("", normalizeDensity(null));
    assertEquals("", normalizeDensity(" "));
    assertEquals("compact", normalizeDensity(" Compact "));
    assertEquals("cozy", normalizeDensity("cozy"));
    assertEquals("spacious", normalizeDensity("SPACIOUS"));
    assertEquals("auto", normalizeDensity("unknown"));
  }

  @Test
  void normalizeScalarSettingsTrimsStringsAndClampsSupportedRanges() {
    assertEquals("darklaf", normalizeString("  darklaf  "));
    assertEquals("", normalizeString(null));
    assertEquals(0, clampPercent(-1));
    assertEquals(100, clampPercent(250));
    assertEquals(8, clampUiFontSize(2));
    assertEquals(48, clampUiFontSize(72));
    assertEquals(0, clampCornerRadius(-4));
    assertEquals(20, clampCornerRadius(99));
  }

  @Test
  void lastSelectedTargetRoundTripsValidMapAndIgnoresInvalidShapes() {
    LastSelectedTarget selected = new LastSelectedTarget("libera", "#ircafe");

    assertEquals(
        Map.of("serverId", "libera", "target", "#ircafe"), serializeLastSelectedTarget(selected));
    assertEquals(
        Optional.of(selected),
        parseLastSelectedTarget(Map.of("serverId", "libera", "target", "#ircafe")));
    assertEquals(Optional.empty(), parseLastSelectedTarget(Map.of("serverId", "libera")));
    assertEquals(Optional.empty(), parseLastSelectedTarget("libera/#ircafe"));
  }
}
