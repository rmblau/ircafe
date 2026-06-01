package cafe.woden.ircclient.config.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeConfigDocumentStoreTest {

  @TempDir Path tempDir;

  @Test
  void remembersWhetherConfigFileExistedAtStartup() throws Exception {
    Path existing = tempDir.resolve("existing.yml");
    Files.writeString(existing, "ircafe: {}\n");

    RuntimeConfigDocumentStore existingStore = new RuntimeConfigDocumentStore(existing);
    RuntimeConfigDocumentStore missingStore =
        new RuntimeConfigDocumentStore(tempDir.resolve("missing.yml"));

    assertTrue(existingStore.fileExistedOnStartup());
    assertFalse(missingStore.fileExistedOnStartup());
  }

  @Test
  void writesDocumentAndCreatesParentDirectories() throws Exception {
    Path config = tempDir.resolve("nested/runtime/ircafe.yml");
    RuntimeConfigDocumentStore store = new RuntimeConfigDocumentStore(config);
    Map<String, Object> doc = new LinkedHashMap<>();
    doc.put("ircafe", Map.of("theme", "dark"));

    store.write(doc);

    assertTrue(Files.exists(config));
    assertEquals(Map.of("theme", "dark"), store.load().get("ircafe"));
  }

  @Test
  void mutationBatchDefersDiskWriteUntilBatchEnds() throws Exception {
    Path config = tempDir.resolve("batched.yml");
    RuntimeConfigDocumentStore store = new RuntimeConfigDocumentStore(config);
    Map<String, Object> doc = new LinkedHashMap<>();
    doc.put("value", "before");

    store.beginMutationBatch();
    store.write(doc);
    assertFalse(Files.exists(config));

    store.load().put("value", "after");
    store.write(store.load());
    store.endMutationBatch();

    assertTrue(Files.exists(config));
    assertEquals("after", store.load().get("value"));
  }
}
