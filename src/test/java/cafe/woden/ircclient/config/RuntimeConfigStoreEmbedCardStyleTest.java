package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeConfigStoreEmbedCardStyleTest {

  @TempDir Path tempDir;

  @Test
  void rememberEmbedCardStylePersistsTokenUnderUiSection() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigStore store = RuntimeConfigStoreTestFixtures.store(cfg);

    store.rememberEmbedCardStyle("glassy");

    String yaml = Files.readString(cfg);
    assertTrue(yaml.contains("ui:"));
    assertTrue(yaml.contains("embedCardStyle: glassy"));
  }

  @Test
  void blankEmbedCardStyleFallsBackToDefaultToken() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigStore store = RuntimeConfigStoreTestFixtures.store(cfg);

    store.rememberEmbedCardStyle("   ");

    String yaml = Files.readString(cfg);
    assertTrue(yaml.contains("embedCardStyle: default"));
  }
}
