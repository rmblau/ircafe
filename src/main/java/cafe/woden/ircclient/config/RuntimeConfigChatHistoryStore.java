package cafe.woden.ircclient.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns chat history and transcript settings under {@code ircafe.ui}. */
class RuntimeConfigChatHistoryStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigChatHistoryStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigChatHistoryStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberInitialLoadLines(int lines) {
    rememberScalarSetting(
        "chatHistoryInitialLoadLines", Math.max(0, lines), "chat history initial load");
  }

  synchronized void rememberPageSize(int pageSize) {
    rememberScalarSetting("chatHistoryPageSize", Math.max(1, pageSize), "chat history page size");
  }

  synchronized void rememberAutoLoadWheelDebounceMs(int debounceMs) {
    int v = Math.max(100, Math.min(30_000, debounceMs));
    rememberScalarSetting("chatHistoryAutoLoadWheelDebounceMs", v, "chat history wheel debounce");
  }

  synchronized void rememberLoadOlderChunkSize(int chunkSize) {
    int v = Math.max(1, Math.min(500, chunkSize));
    rememberScalarSetting("chatHistoryLoadOlderChunkSize", v, "chat history load-older chunk-size");
  }

  synchronized void rememberLoadOlderChunkDelayMs(int chunkDelayMs) {
    int v = Math.max(0, Math.min(1_000, chunkDelayMs));
    rememberScalarSetting(
        "chatHistoryLoadOlderChunkDelayMs", v, "chat history load-older chunk-delay");
  }

  synchronized void rememberLoadOlderChunkEdtBudgetMs(int chunkEdtBudgetMs) {
    int v = Math.max(1, Math.min(33, chunkEdtBudgetMs));
    rememberScalarSetting(
        "chatHistoryLoadOlderChunkEdtBudgetMs", v, "chat history load-older EDT budget");
  }

  synchronized void rememberDeferRichTextDuringBatch(boolean enabled) {
    rememberScalarSetting(
        "chatHistoryDeferRichTextDuringBatch", enabled, "chat history deferred-rich-text");
  }

  synchronized boolean readSmoothWheelScrollingEnabled(boolean defaultValue) {
    return readUiBoolean(
        "chatSmoothWheelScrollingEnabled", defaultValue, "ui.chatSmoothWheelScrollingEnabled");
  }

  synchronized void rememberSmoothWheelScrollingEnabled(boolean enabled) {
    rememberScalarSetting(
        "chatSmoothWheelScrollingEnabled", enabled, "chat smooth-wheel scrolling");
  }

  synchronized boolean readLockViewportDuringLoadOlder(boolean defaultValue) {
    return readUiBoolean(
        "chatHistoryLockViewportDuringLoadOlder",
        defaultValue,
        "ui.chatHistoryLockViewportDuringLoadOlder");
  }

  synchronized void rememberLockViewportDuringLoadOlder(boolean enabled) {
    rememberScalarSetting(
        "chatHistoryLockViewportDuringLoadOlder", enabled, "chat history viewport-lock");
  }

  synchronized void rememberRemoteRequestTimeoutSeconds(int seconds) {
    int v = Math.max(1, Math.min(120, seconds));
    rememberScalarSetting(
        "chatHistoryRemoteRequestTimeoutSeconds", v, "chat history remote-timeout");
  }

  synchronized void rememberRemoteZncPlaybackTimeoutSeconds(int seconds) {
    int v = Math.max(1, Math.min(300, seconds));
    rememberScalarSetting(
        "chatHistoryRemoteZncPlaybackTimeoutSeconds", v, "chat history remote ZNC-timeout");
  }

  synchronized void rememberRemoteZncPlaybackWindowMinutes(int minutes) {
    int v = Math.max(1, Math.min(1440, minutes));
    rememberScalarSetting(
        "chatHistoryRemoteZncPlaybackWindowMinutes", v, "chat history remote ZNC window");
  }

  synchronized void rememberCommandHistoryMaxSize(int maxSize) {
    int v = maxSize;
    if (v <= 0) v = 500;
    if (v > 500) v = 500;
    rememberScalarSetting("commandHistoryMaxSize", v, "command history max size");
  }

  synchronized void rememberTranscriptMaxLinesPerTarget(int maxLines) {
    int v = Math.max(0, maxLines);
    if (v > 200_000) v = 200_000;
    rememberScalarSetting(
        "chatTranscriptMaxLinesPerTarget", v, "chat transcript max-lines-per-target");
  }

  private boolean readUiBoolean(String key, boolean defaultValue, String description) {
    return readUiValue(description, key)
        .flatMap(RuntimeConfigChatHistoryStore::asBoolean)
        .orElse(defaultValue);
  }

  private Optional<Object> readUiValue(String description, String... path) {
    String[] fullPath = new String[path.length + 2];
    fullPath[0] = "ircafe";
    fullPath[1] = "ui";
    System.arraycopy(path, 0, fullPath, 2, path.length);
    return readExistingConfigValue(description, fullPath);
  }

  private Optional<Object> readExistingConfigValue(String description, String... path) {
    try {
      if (file.toString().isBlank()) return Optional.empty();
      if (!Files.exists(file)) return Optional.empty();

      Map<String, Object> doc = documentStore.load();
      return RuntimeConfigDocumentPathReader.readValue(doc, path);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read {} from '{}'", description, file, e);
      return Optional.empty();
    }
  }

  private static Optional<Boolean> asBoolean(Object value) {
    if (value instanceof Boolean b) return Optional.of(b);
    if (value instanceof String s) {
      String t = s.trim();
      if (t.equalsIgnoreCase("true")) return Optional.of(Boolean.TRUE);
      if (t.equalsIgnoreCase("false")) return Optional.of(Boolean.FALSE);
    }
    if (value instanceof Number n) {
      int i = n.intValue();
      if (i == 0) return Optional.of(Boolean.FALSE);
      if (i == 1) return Optional.of(Boolean.TRUE);
    }
    return Optional.empty();
  }

  private void rememberScalarSetting(String key, Object value, String description) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = getOrCreateMapPath(doc, "ircafe", "ui");

      ui.put(key, value);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} setting to '{}'", description, file, e);
    }
  }

  private static Map<String, Object> getOrCreateMapPath(Map<String, Object> root, String... path) {
    Map<String, Object> current = root;
    for (String segment : path) {
      current = getOrCreateMap(current, segment);
    }
    return current;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }
}
