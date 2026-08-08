package cafe.woden.ircclient.config.runtime.launch;

import static cafe.woden.ircclient.config.runtime.launch.RuntimeConfigLaunchJvmSettingsCodec.isEmptyJvmSettingValue;
import static cafe.woden.ircclient.config.runtime.launch.RuntimeConfigLaunchJvmSettingsCodec.normalizeArgs;
import static cafe.woden.ircclient.config.runtime.launch.RuntimeConfigLaunchJvmSettingsCodec.normalizeGc;
import static cafe.woden.ircclient.config.runtime.launch.RuntimeConfigLaunchJvmSettingsCodec.normalizeHeapMiB;
import static cafe.woden.ircclient.config.runtime.launch.RuntimeConfigLaunchJvmSettingsCodec.normalizeJavaCommandFallback;
import static cafe.woden.ircclient.config.runtime.launch.RuntimeConfigLaunchJvmSettingsCodec.normalizeJavaCommandReadValue;
import static cafe.woden.ircclient.config.runtime.launch.RuntimeConfigLaunchJvmSettingsCodec.normalizeJavaCommandSetting;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RuntimeConfigLaunchJvmSettingsCodecTest {

  @Test
  void normalizeJavaCommandHandlesFallbacksReadsAndDefaultLikeWrites() {
    assertEquals("java", normalizeJavaCommandFallback(""));
    assertEquals("java21", normalizeJavaCommandFallback(" java21 "));
    assertEquals("java25", normalizeJavaCommandReadValue(" java25 "));
    assertEquals("", normalizeJavaCommandSetting(" java "));
    assertEquals("", normalizeJavaCommandSetting(" "));
    assertEquals("java25", normalizeJavaCommandSetting(" java25 "));
  }

  @Test
  void normalizeHeapMiBClampsToSupportedRange() {
    assertEquals(0, normalizeHeapMiB(-1));
    assertEquals(512, normalizeHeapMiB(512));
    assertEquals(262_144, normalizeHeapMiB(999_999));
  }

  @Test
  void normalizeGcMapsKnownAliasesAndRejectsUnknownValues() {
    assertEquals("", normalizeGc("default"));
    assertEquals("g1", normalizeGc("UseG1GC"));
    assertEquals("zgc", normalizeGc("UseZGC"));
    assertEquals("shenandoah", normalizeGc("ShenandoahGC"));
    assertEquals("parallel", normalizeGc("UseParallel"));
    assertEquals("serial", normalizeGc("SerialGC"));
    assertEquals("epsilon", normalizeGc("UseEpsilonGC"));
    assertEquals("", normalizeGc("unknown"));
  }

  @Test
  void normalizeArgsTrimsAndDropsBlankEntries() {
    assertEquals(
        List.of("-Xmx1g", "-Dsample=true"),
        normalizeArgs(List.of(" -Xmx1g ", "", "-Dsample=true")));
  }

  @Test
  void emptyJvmSettingValuesRecognizeBlankZeroAndEmptyCollections() {
    assertTrue(isEmptyJvmSettingValue(null));
    assertTrue(isEmptyJvmSettingValue(" "));
    assertTrue(isEmptyJvmSettingValue(0));
    assertTrue(isEmptyJvmSettingValue(List.of()));
    assertFalse(isEmptyJvmSettingValue("java25"));
    assertFalse(isEmptyJvmSettingValue(1));
    assertFalse(isEmptyJvmSettingValue(List.of("-Dsample=true")));
  }
}
