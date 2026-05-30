package cafe.woden.ircclient.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Owns low-level runtime config document IO and write batching.
 *
 * <p>{@link RuntimeConfigStore} still owns the domain-specific document shape; this class only
 * knows how to load, write, and coalesce mutations for the YAML document backing the store.
 */
class RuntimeConfigDocumentStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigDocumentStore.class);

  private final Path file;
  private final Yaml yaml;
  private final boolean fileExistedOnStartup;

  private int mutationBatchDepth = 0;
  private Map<String, Object> mutationBatchDoc = null;
  private boolean mutationBatchDirty = false;

  RuntimeConfigDocumentStore(Path file) {
    this.file = file;
    this.fileExistedOnStartup = existsSafely(file);
    this.yaml = new Yaml(dumperOptions());
  }

  boolean fileExistedOnStartup() {
    return fileExistedOnStartup;
  }

  synchronized void runMutationBatch(Runnable action) {
    if (action == null) return;
    beginMutationBatch();
    try {
      action.run();
    } finally {
      endMutationBatch();
    }
  }

  synchronized void beginMutationBatch() {
    if (mutationBatchDepth == 0) {
      try {
        mutationBatchDoc = loadOrEmpty();
      } catch (Exception e) {
        mutationBatchDoc = new LinkedHashMap<>();
        log.warn("[ircafe] Could not start mutation batch for '{}'", file, e);
      }
      mutationBatchDirty = false;
    }
    mutationBatchDepth++;
  }

  synchronized void endMutationBatch() {
    if (mutationBatchDepth <= 0) return;
    mutationBatchDepth--;
    if (mutationBatchDepth > 0) return;
    try {
      if (mutationBatchDirty && mutationBatchDoc != null) {
        writeNow(mutationBatchDoc);
      }
    } catch (Exception e) {
      log.warn("[ircafe] Could not flush mutation batch to '{}'", file, e);
    } finally {
      mutationBatchDoc = null;
      mutationBatchDirty = false;
    }
  }

  @SuppressWarnings("unchecked")
  synchronized Map<String, Object> load() throws IOException {
    if (mutationBatchDepth > 0 && mutationBatchDoc != null) {
      return mutationBatchDoc;
    }
    try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      Object o = yaml.load(r);
      if (o instanceof Map<?, ?> m) {
        return (Map<String, Object>) m;
      }
      return new LinkedHashMap<>();
    }
  }

  synchronized Map<String, Object> loadOrEmpty() throws IOException {
    return Files.exists(file) ? load() : new LinkedHashMap<>();
  }

  synchronized void write(Map<String, Object> doc) throws IOException {
    if (mutationBatchDepth > 0) {
      mutationBatchDoc = (doc == null) ? new LinkedHashMap<>() : doc;
      mutationBatchDirty = true;
      return;
    }
    writeNow(doc);
  }

  synchronized void writeNow(Map<String, Object> doc) throws IOException {
    Path parent = file.getParent();
    if (parent != null && !Files.exists(parent)) {
      Files.createDirectories(parent);
    }
    try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
      yaml.dump(doc, w);
    }
  }

  private static boolean existsSafely(Path file) {
    try {
      return file != null && !file.toString().isBlank() && Files.exists(file);
    } catch (Exception ignored) {
      return false;
    }
  }

  private static DumperOptions dumperOptions() {
    DumperOptions opts = new DumperOptions();
    opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    opts.setPrettyFlow(true);
    opts.setIndent(2);
    // SnakeYAML requires indicatorIndent < indent.
    // With indent=2, indicatorIndent=1 keeps list indicators aligned nicely.
    opts.setIndicatorIndent(1);
    opts.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    return opts;
  }
}
