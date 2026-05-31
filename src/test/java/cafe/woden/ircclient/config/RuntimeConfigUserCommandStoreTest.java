package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.model.UserCommandAlias;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeConfigUserCommandStoreTest {

  @TempDir Path tempDir;

  @Test
  void aliasesRoundTripThroughStore() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigUserCommandStore store = store(cfg);

    store.rememberAliases(
        List.of(
            new UserCommandAlias(true, " hi ", "/msg %1 hello"),
            new UserCommandAlias(false, "wave", "/me waves")));

    assertEquals(
        List.of(
            new UserCommandAlias(true, "hi", "/msg %1 hello"),
            new UserCommandAlias(false, "wave", "/me waves")),
        store.readAliases());

    String yaml = Files.readString(cfg);
    assertTrue(yaml.contains("commands"));
    assertTrue(yaml.contains("aliases"));
    assertTrue(yaml.contains("name: hi"));
    assertTrue(yaml.contains("template: /msg %1 hello"));
  }

  @Test
  void readAliasesAcceptsLegacyExpansionKey() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    Files.writeString(
        cfg,
        """
        ircafe:
          commands:
            aliases:
              - enabled: false
                name: legacy
                expansion: /msg %1 hello
        """);
    RuntimeConfigUserCommandStore store = store(cfg);

    assertEquals(
        List.of(new UserCommandAlias(false, "legacy", "/msg %1 hello")), store.readAliases());
  }

  @Test
  void unknownCommandAsRawFallsBackForMissingOrInvalidValues() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigUserCommandStore store = store(cfg);

    assertTrue(store.readUnknownCommandAsRawEnabled(true));
    assertFalse(store.readUnknownCommandAsRawEnabled(false));

    Files.writeString(
        cfg,
        """
        ircafe:
          commands:
            unknownCommandAsRaw: maybe
        """);

    assertTrue(store.readUnknownCommandAsRawEnabled(true));
    assertFalse(store.readUnknownCommandAsRawEnabled(false));
  }

  @Test
  void unknownCommandAsRawPersistsAndReadsBack() {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigUserCommandStore store = store(cfg);

    store.rememberUnknownCommandAsRawEnabled(true);
    assertTrue(store.readUnknownCommandAsRawEnabled(false));

    store.rememberUnknownCommandAsRawEnabled(false);
    assertFalse(store.readUnknownCommandAsRawEnabled(true));
  }

  private static RuntimeConfigUserCommandStore store(Path cfg) {
    return new RuntimeConfigUserCommandStore(cfg, new RuntimeConfigDocumentStore(cfg));
  }
}
