package cafe.woden.ircclient.config;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns outgoing message presentation settings under {@code ircafe.ui}. */
class RuntimeConfigOutgoingMessageStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigOutgoingMessageStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigOutgoingMessageStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberClientLineColorEnabled(boolean enabled) {
    rememberScalarSetting("clientLineColorEnabled", enabled, "outgoing message color enabled");
  }

  synchronized void rememberClientLineColor(String hex) {
    rememberScalarSetting(
        "clientLineColor", Objects.toString(hex, "").trim(), "outgoing message color");
  }

  synchronized void rememberOutgoingDeliveryIndicatorsEnabled(boolean enabled) {
    rememberScalarSetting(
        "outgoingDeliveryIndicatorsEnabled", enabled, "outgoing delivery indicators");
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
