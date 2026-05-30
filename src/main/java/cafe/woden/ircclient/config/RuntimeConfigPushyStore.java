package cafe.woden.ircclient.config;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns Pushy notification settings under {@code ircafe.pushy}. */
class RuntimeConfigPushyStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigPushyStore.class);
  private static final String DEFAULT_ENDPOINT = "https://api.pushy.me/push";
  private static final String DEFAULT_TITLE_PREFIX = "IRCafe";

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigPushyStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberSettings(PushyProperties settings) {
    try {
      if (file.toString().isBlank()) return;

      PushyProperties safe =
          settings != null
              ? settings
              : new PushyProperties(false, null, null, null, null, null, null, null);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> pushy = getOrCreateMapPath(doc, "ircafe", "pushy");

      pushy.put("enabled", safe.enabled());
      rememberOptionalString(pushy, "endpoint", safe.endpoint(), DEFAULT_ENDPOINT);
      rememberOptionalString(pushy, "apiKey", safe.apiKey(), null);
      rememberOptionalString(pushy, "deviceToken", safe.deviceToken(), null);
      rememberOptionalString(pushy, "topic", safe.topic(), null);
      rememberOptionalString(pushy, "titlePrefix", safe.titlePrefix(), DEFAULT_TITLE_PREFIX);
      pushy.put("connectTimeoutSeconds", safe.connectTimeoutSeconds());
      pushy.put("readTimeoutSeconds", safe.readTimeoutSeconds());

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist pushy settings to '{}'", file, e);
    }
  }

  private static void rememberOptionalString(
      Map<String, Object> target, String key, String value, String defaultValue) {
    String normalized = Objects.toString(value, "").trim();
    if (normalized.isEmpty() || normalized.equals(defaultValue)) {
      target.remove(key);
    } else {
      target.put(key, normalized);
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
