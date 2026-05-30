package cafe.woden.ircclient.config;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns timestamp settings under {@code ircafe.ui.timestamps}. */
class RuntimeConfigTimestampStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigTimestampStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigTimestampStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberEnabled(boolean enabled) {
    rememberSetting("enabled", enabled);
  }

  synchronized void rememberFormat(String format) {
    String fmt = (format == null || format.isBlank()) ? "HH:mm:ss" : format.trim();
    rememberSetting("format", fmt);
  }

  synchronized void rememberIncludeChatMessages(boolean includeChatMessages) {
    rememberSetting("includeChatMessages", includeChatMessages);
  }

  synchronized void rememberIncludePresenceMessages(boolean includePresenceMessages) {
    rememberSetting("includePresenceMessages", includePresenceMessages);
  }

  private void rememberSetting(String key, Object value) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = getOrCreateMapPath(doc, "ircafe", "ui");
      Map<String, Object> timestamps = getOrCreateMap(ui, "timestamps");

      timestamps.put(key, value);
      // Clean up legacy flat key.
      ui.remove("chatMessageTimestampsEnabled");

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist timestamp {} setting to '{}'", key, file, e);
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
