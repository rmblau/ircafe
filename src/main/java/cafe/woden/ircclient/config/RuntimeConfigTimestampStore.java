package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMapPath;

import java.nio.file.Path;
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

}
