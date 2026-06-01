package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.runtime.launch.RuntimeConfigLaunchJvmStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeConfigLaunchJvmStoreTest {

  @TempDir Path tempDir;

  @Test
  void launchJvmSettingsRoundTripThroughStore() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigLaunchJvmStore store = store(cfg);

    store.rememberJavaCommand("java21");
    store.rememberXmsMiB(768);
    store.rememberXmxMiB(4096);
    store.rememberGc("UseZGC");
    store.rememberArgs(List.of(" -XX:+AlwaysPreTouch ", "", "-Dsample=true"));

    assertEquals("java21", store.readJavaCommand("java"));
    assertEquals(768, store.readXmsMiB(0));
    assertEquals(4096, store.readXmxMiB(0));
    assertEquals("zgc", store.readGc(""));
    assertEquals(List.of("-XX:+AlwaysPreTouch", "-Dsample=true"), store.readArgs(List.of()));

    String yaml = Files.readString(cfg);
    assertTrue(yaml.contains("launch"));
    assertTrue(yaml.contains("jvm"));
    assertTrue(yaml.contains("javaCommand: java21"));
    assertTrue(yaml.contains("xmsMiB: 768"));
    assertTrue(yaml.contains("xmxMiB: 4096"));
    assertTrue(yaml.contains("gc: zgc"));
    assertTrue(yaml.contains("-XX:+AlwaysPreTouch"));
  }

  @Test
  void defaultLikeLaunchJvmValuesAreCompactedOutOfConfig() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigLaunchJvmStore store = store(cfg);

    store.rememberJavaCommand("java21");
    store.rememberXmsMiB(512);
    store.rememberXmxMiB(1024);
    store.rememberGc("g1");
    store.rememberArgs(List.of("-Dfoo=bar"));

    store.rememberJavaCommand("java");
    store.rememberXmsMiB(0);
    store.rememberXmxMiB(0);
    store.rememberGc("default");
    store.rememberArgs(List.of());

    String yaml = Files.readString(cfg);
    assertFalse(yaml.contains("launch:"));
    assertFalse(yaml.contains("javaCommand:"));
    assertFalse(yaml.contains("xmsMiB:"));
    assertFalse(yaml.contains("xmxMiB:"));
    assertFalse(yaml.contains("gc:"));
    assertFalse(yaml.contains("args:"));
  }

  @Test
  void readLaunchJvmSettingsFallsBackForMissingOrInvalidValues() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    Files.writeString(
        cfg,
        "ircafe:\n"
            + "  launch:\n"
            + "    jvm:\n"
            + "      javaCommand: ' '\n"
            + "      xmsMiB: nope\n"
            + "      xmxMiB: 999999\n"
            + "      gc: unknown\n"
            + "      args: not-a-list\n");
    RuntimeConfigLaunchJvmStore store = store(cfg);

    assertEquals("java", store.readJavaCommand("java"));
    assertEquals(512, store.readXmsMiB(512));
    assertEquals(262_144, store.readXmxMiB(1024));
    assertEquals("", store.readGc("g1"));
    assertEquals(List.of("-Dfallback=true"), store.readArgs(List.of("-Dfallback=true")));
  }

  private static RuntimeConfigLaunchJvmStore store(Path cfg) {
    return new RuntimeConfigLaunchJvmStore(cfg, new RuntimeConfigDocumentStore(cfg));
  }
}
