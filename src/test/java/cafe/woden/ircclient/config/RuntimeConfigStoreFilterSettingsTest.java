package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeConfigStoreFilterSettingsTest {

  @TempDir Path tempDir;

  @Test
  void clampsPersistedFilterPlaceholderTuning() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigStore store = RuntimeConfigStoreTestFixtures.store(cfg);

    store.rememberFilterPlaceholderMaxPreviewLines(99);
    store.rememberFilterPlaceholderMaxLinesPerRun(100_000);
    store.rememberFilterPlaceholderTooltipMaxTags(700);
    store.rememberFilterHistoryPlaceholderMaxRunsPerBatch(8_000);

    String yaml = Files.readString(cfg);
    assertTrue(yaml.contains("placeholderMaxPreviewLines: 25"));
    assertTrue(yaml.contains("placeholderMaxLinesPerRun: 50000"));
    assertTrue(yaml.contains("placeholderTooltipMaxTags: 500"));
    assertTrue(yaml.contains("historyPlaceholderMaxRunsPerBatch: 5000"));
  }
}
