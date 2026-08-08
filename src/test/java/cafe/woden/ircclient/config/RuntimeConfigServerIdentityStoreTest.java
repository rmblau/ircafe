package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerIdentityStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeConfigServerIdentityStoreTest {

  @TempDir Path tempDir;

  @Test
  void rememberNickTrimsAndPersistsNonBlankPreferredNick() throws Exception {
    Path cfg = configWithServer();
    RuntimeConfigServerIdentityStore store =
        new RuntimeConfigServerIdentityStore(cfg, new RuntimeConfigDocumentStore(cfg));

    store.rememberNick("libera", " alice ");

    assertTrue(Files.readString(cfg).contains("nick: alice"));
  }

  @Test
  void rememberNickIgnoresBlankPreferredNick() throws Exception {
    Path cfg = configWithServer();
    RuntimeConfigServerIdentityStore store =
        new RuntimeConfigServerIdentityStore(cfg, new RuntimeConfigDocumentStore(cfg));

    store.rememberNick("libera", " ");

    assertFalse(Files.readString(cfg).contains("nick:"));
  }

  private Path configWithServer() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    Files.writeString(
        cfg,
        """
        irc:
          servers:
            - id: libera
              host: irc.libera.chat
        """);
    return cfg;
  }
}
