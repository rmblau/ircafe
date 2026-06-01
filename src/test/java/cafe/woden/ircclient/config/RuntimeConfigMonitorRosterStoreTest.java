package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.config.runtime.server.RuntimeConfigMonitorRosterStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeConfigMonitorRosterStoreTest {

  @TempDir Path tempDir;

  @Test
  void persistsUpdatesAndClearsMonitorNickList() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    Files.writeString(
        cfg,
        """
        irc:
          servers:
            - id: libera
        """);
    RuntimeConfigMonitorRosterStore store = monitorRosterStore(cfg);

    store.rememberMonitorNick("libera", "Alice");
    store.rememberMonitorNick("libera", "alice");
    store.rememberMonitorNick("libera", "bob!ident@host");
    assertEquals(List.of("Alice", "bob"), store.readMonitorNicks("libera"));

    store.forgetMonitorNick("libera", "ALICE");
    assertEquals(List.of("bob"), store.readMonitorNicks("libera"));

    store.replaceMonitorNicks("libera", List.of("charlie", ":dave,extra", "#channel", "charlie"));
    assertEquals(List.of("charlie", "dave"), store.readMonitorNicks("libera"));

    store.replaceMonitorNicks("libera", List.of());
    assertEquals(List.of(), store.readMonitorNicks("libera"));
  }

  @Test
  void doesNotCreateMissingServersWhenRememberingNicks() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    Files.writeString(cfg, "irc:\n  servers: []\n");
    RuntimeConfigMonitorRosterStore store = monitorRosterStore(cfg);

    store.rememberMonitorNick("missing", "Alice");

    assertEquals(List.of(), store.readMonitorNicks("missing"));
    assertEquals("irc:\n  servers: []\n", Files.readString(cfg));
  }

  private static RuntimeConfigMonitorRosterStore monitorRosterStore(Path cfg) {
    return new RuntimeConfigMonitorRosterStore(cfg, new RuntimeConfigDocumentStore(cfg));
  }
}
