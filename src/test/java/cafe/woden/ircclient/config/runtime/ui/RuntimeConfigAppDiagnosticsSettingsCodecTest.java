package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigAppDiagnosticsSettingsCodec.normalizeArgs;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigAppDiagnosticsSettingsCodec.normalizeAssertjFallbackViolationReportMs;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigAppDiagnosticsSettingsCodec.normalizeAssertjFreezeThresholdMs;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigAppDiagnosticsSettingsCodec.normalizeAssertjWatchdogPollMs;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigAppDiagnosticsSettingsCodec.normalizeJavaCommandFallback;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigAppDiagnosticsSettingsCodec.normalizeString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class RuntimeConfigAppDiagnosticsSettingsCodecTest {

  @Test
  void normalizeAssertjSwingThresholdsClampToSupportedRanges() {
    assertEquals(500, normalizeAssertjFreezeThresholdMs(100));
    assertEquals(10_000, normalizeAssertjFreezeThresholdMs(10_000));
    assertEquals(120_000, normalizeAssertjFreezeThresholdMs(500_000));

    assertEquals(100, normalizeAssertjWatchdogPollMs(50));
    assertEquals(5_000, normalizeAssertjWatchdogPollMs(5_000));
    assertEquals(10_000, normalizeAssertjWatchdogPollMs(50_000));

    assertEquals(250, normalizeAssertjFallbackViolationReportMs(100));
    assertEquals(5_000, normalizeAssertjFallbackViolationReportMs(5_000));
    assertEquals(120_000, normalizeAssertjFallbackViolationReportMs(500_000));
  }

  @Test
  void normalizeStringAndJavaFallbackTrimValues() {
    assertEquals("tools/jhiccup.jar", normalizeString(" tools/jhiccup.jar "));
    assertEquals("", normalizeString(null));
    assertEquals("java", normalizeJavaCommandFallback(""));
    assertEquals("java21", normalizeJavaCommandFallback(" java21 "));
  }

  @Test
  void normalizeArgsTrimsAndDropsBlankEntries() {
    assertEquals(
        List.of("-Xmx64m", "-Dsample=true"),
        normalizeArgs(List.of(" -Xmx64m ", "", "-Dsample=true")));
  }
}
