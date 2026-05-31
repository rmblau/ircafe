package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigCtcpAutoReplyStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeConfigCtcpAutoReplyStoreTest {

  @TempDir Path tempDir;

  @Test
  void ctcpAutoReplySettingsRoundTripThroughStore() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigCtcpAutoReplyStore store = store(cfg);

    store.rememberEnabled(false);
    store.rememberVersionEnabled(false);
    store.rememberPingEnabled(true);
    store.rememberTimeEnabled(false);

    assertFalse(store.readEnabled(true));
    assertFalse(store.readVersionEnabled(true));
    assertTrue(store.readPingEnabled(false));
    assertFalse(store.readTimeEnabled(true));

    String yaml = Files.readString(cfg);
    assertTrue(yaml.contains("ctcpReplies"));
    assertTrue(yaml.contains("enabled: false"));
    assertTrue(yaml.contains("version: false"));
    assertTrue(yaml.contains("ping: true"));
    assertTrue(yaml.contains("time: false"));
  }

  @Test
  void readCtcpAutoReplySettingsFallsBackForMissingOrInvalidValues() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    Files.writeString(
        cfg,
        "ircafe:\n"
            + "  ui:\n"
            + "    ctcpReplies:\n"
            + "      enabled: maybe\n"
            + "      version: 1\n"
            + "      ping: 0\n"
            + "      time:\n"
            + "        nested: invalid\n");
    RuntimeConfigCtcpAutoReplyStore store = store(cfg);

    assertTrue(store.readEnabled(true));
    assertTrue(store.readVersionEnabled(false));
    assertFalse(store.readPingEnabled(true));
    assertTrue(store.readTimeEnabled(true));
  }

  private static RuntimeConfigCtcpAutoReplyStore store(Path cfg) {
    return new RuntimeConfigCtcpAutoReplyStore(cfg, new RuntimeConfigDocumentStore(cfg));
  }
}
