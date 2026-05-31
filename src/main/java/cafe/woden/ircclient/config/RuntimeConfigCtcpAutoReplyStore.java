package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMapPath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns CTCP auto-reply settings under {@code ircafe.ui.ctcpReplies}. */
class RuntimeConfigCtcpAutoReplyStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigCtcpAutoReplyStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigCtcpAutoReplyStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized boolean readEnabled(boolean defaultValue) {
    return readBoolean("enabled", defaultValue);
  }

  synchronized boolean readVersionEnabled(boolean defaultValue) {
    return readBoolean("version", defaultValue);
  }

  synchronized boolean readPingEnabled(boolean defaultValue) {
    return readBoolean("ping", defaultValue);
  }

  synchronized boolean readTimeEnabled(boolean defaultValue) {
    return readBoolean("time", defaultValue);
  }

  synchronized void rememberEnabled(boolean enabled) {
    rememberBoolean("enabled", enabled);
  }

  synchronized void rememberVersionEnabled(boolean enabled) {
    rememberBoolean("version", enabled);
  }

  synchronized void rememberPingEnabled(boolean enabled) {
    rememberBoolean("ping", enabled);
  }

  synchronized void rememberTimeEnabled(boolean enabled) {
    rememberBoolean("time", enabled);
  }

  private boolean readBoolean(String key, boolean defaultValue) {
    try {
      if (file.toString().isBlank()) return defaultValue;
      if (!Files.exists(file)) return defaultValue;

      Map<String, Object> doc = documentStore.load();
      return RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "ui", "ctcpReplies", key)
          .flatMap(RuntimeConfigYamlSupport::asBoolean)
          .orElse(defaultValue);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read ui.ctcpReplies.{} from '{}'", key, file, e);
      return defaultValue;
    }
  }

  private void rememberBoolean(String key, boolean enabled) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ctcpReplies = getOrCreateMapPath(doc, "ircafe", "ui", "ctcpReplies");

      ctcpReplies.put(key, enabled);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ui.ctcpReplies.{} setting to '{}'", key, file, e);
    }
  }

}
