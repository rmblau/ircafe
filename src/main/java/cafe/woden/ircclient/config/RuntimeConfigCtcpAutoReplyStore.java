package cafe.woden.ircclient.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
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
          .flatMap(RuntimeConfigCtcpAutoReplyStore::asBoolean)
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
