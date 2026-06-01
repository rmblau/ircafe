package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.runtime.server.RuntimeConfigPrivateMessageTargetStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeConfigPrivateMessageTargetStoreTest {

  @TempDir Path tempDir;

  @Test
  void persistsPrivateMessageTargetsInAutoJoinWithoutDuplicatingByCase() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    Files.writeString(
        cfg,
        """
        irc:
          servers:
            - id: libera
              autoJoin:
                - "#java"
        """);
    RuntimeConfigPrivateMessageTargetStore store = privateMessageTargetStore(cfg);

    store.rememberPrivateMessageTarget("libera", "Alice");
    store.rememberPrivateMessageTarget("libera", "alice");

    assertEquals(List.of("Alice"), store.readPrivateMessageTargets("libera"));
    String yaml = Files.readString(cfg);
    assertTrue(yaml.contains("#java"));
    assertTrue(yaml.contains("query:Alice"));
  }

  @Test
  void forgetPrivateMessageTargetPreservesChannelAutoJoinEntries() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    Files.writeString(
        cfg,
        """
        irc:
          servers:
            - id: libera
              autoJoin:
                - "#java"
                - "query:Alice"
                - "QUERY:bob"
        """);
    RuntimeConfigPrivateMessageTargetStore store = privateMessageTargetStore(cfg);

    store.forgetPrivateMessageTarget("libera", "ALICE");

    assertEquals(List.of("bob"), store.readPrivateMessageTargets("libera"));
    String yaml = Files.readString(cfg);
    assertTrue(yaml.contains("#java"));
    assertTrue(yaml.contains("QUERY:bob"));
  }

  @Test
  void doesNotCreateMissingServersWhenRememberingTargets() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    Files.writeString(cfg, "irc:\n  servers: []\n");
    RuntimeConfigPrivateMessageTargetStore store = privateMessageTargetStore(cfg);

    store.rememberPrivateMessageTarget("missing", "Alice");

    assertEquals(List.of(), store.readPrivateMessageTargets("missing"));
    assertEquals("irc:\n  servers: []\n", Files.readString(cfg));
  }

  private static RuntimeConfigPrivateMessageTargetStore privateMessageTargetStore(Path cfg) {
    return new RuntimeConfigPrivateMessageTargetStore(cfg, new RuntimeConfigDocumentStore(cfg));
  }
}
