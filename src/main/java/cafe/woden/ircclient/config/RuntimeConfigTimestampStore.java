package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.mutateMap;

import java.nio.file.Path;
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
    mutateMap(
        file,
        documentStore,
        log,
        "timestamp " + key + " setting",
        ui -> {
          getOrCreateMap(ui, "timestamps").put(key, value);
          ui.remove("chatMessageTimestampsEnabled");
        },
        "ircafe",
        "ui");
  }
}
