package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeConfigServerListStoreTest {

  @TempDir Path tempDir;

  @Test
  void seedsDefaultServersWhenServersKeyIsMissing() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigServerListStore store =
        serverListStore(
            cfg,
            IrcPropertiesTestFixtures.properties(
                IrcPropertiesTestFixtures.serverBuilder("libera")
                    .host("irc.libera.chat")
                    .autoJoin(List.of("#ircafe"))
                    .build()));

    store.ensureFileExistsWithServers();

    assertTrue(Files.exists(cfg));
    assertEquals(List.of("libera"), store.readServerIds());
    assertEquals(Map.of("libera", List.of("#ircafe")), store.readExplicitServerAutoJoinById());
  }

  @Test
  void readsOnlyExplicitAutoJoinEntries() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    Files.writeString(
        cfg,
        """
        irc:
          servers:
            - id: libera
              autoJoin:
                - "#runtime"
            - id: oftc
              nick: test
        """);
    RuntimeConfigServerListStore store =
        serverListStore(cfg, IrcPropertiesTestFixtures.properties());

    assertEquals(Map.of("libera", List.of("#runtime")), store.readExplicitServerAutoJoinById());
  }

  @Test
  void writeServersReplacesConfiguredServerIds() {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigServerListStore store =
        serverListStore(cfg, IrcPropertiesTestFixtures.properties());

    store.writeServers(
        List.of(
            IrcPropertiesTestFixtures.server("libera"),
            IrcPropertiesTestFixtures.server("oftc")));

    assertEquals(List.of("libera", "oftc"), store.readServerIds());
  }

  private static RuntimeConfigServerListStore serverListStore(Path cfg, IrcProperties defaults) {
    return new RuntimeConfigServerListStore(cfg, new RuntimeConfigDocumentStore(cfg), defaults);
  }
}
