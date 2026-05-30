package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.api.Ircv3CapabilityNameResolverPort;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted IRCv3 capability request overrides under {@code ircafe.ui.ircv3Capabilities}. */
class RuntimeConfigIrcv3CapabilityStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigIrcv3CapabilityStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;
  private Ircv3CapabilityNameResolverPort capabilityNameResolver =
      new Ircv3CapabilityNameResolverPort() {};

  RuntimeConfigIrcv3CapabilityStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  void setCapabilityNameResolver(Ircv3CapabilityNameResolverPort capabilityNameResolver) {
    this.capabilityNameResolver =
        capabilityNameResolver == null
            ? new Ircv3CapabilityNameResolverPort() {}
            : capabilityNameResolver;
  }

  /**
   * Reads persisted IRCv3 capability request overrides under {@code ircafe.ui.ircv3Capabilities}.
   *
   * <p>Keys are normalized to lowercase, values are booleans. Missing/invalid entries are ignored.
   */
  synchronized Map<String, Boolean> readCapabilities() {
    try {
      if (file.toString().isBlank()) return Map.of();
      if (!Files.exists(file)) return Map.of();

      Map<String, Object> doc = documentStore.load();
      Object ircafeObj = doc.get("ircafe");
      if (!(ircafeObj instanceof Map<?, ?> ircafe)) return Map.of();

      Object uiObj = ircafe.get("ui");
      if (!(uiObj instanceof Map<?, ?> ui)) return Map.of();

      Object capsObj = ui.get("ircv3Capabilities");
      if (!(capsObj instanceof Map<?, ?> caps)) return Map.of();

      Map<String, Boolean> out = new LinkedHashMap<>();
      for (Map.Entry<?, ?> e : caps.entrySet()) {
        String key = normalizeCapabilityKey(Objects.toString(e.getKey(), ""));
        if (key == null) continue;
        Optional<Boolean> b = asBoolean(e.getValue());
        b.ifPresent(value -> out.put(key, value));
      }
      return out;
    } catch (Exception e) {
      log.warn("[ircafe] Could not read IRCv3 capability settings from '{}'", file, e);
      return Map.of();
    }
  }

  /**
   * Returns whether a given IRCv3 capability should be requested, falling back to {@code
   * defaultEnabled} when no explicit override is present.
   */
  synchronized boolean isCapabilityEnabled(String capability, boolean defaultEnabled) {
    String key = normalizeCapabilityKey(capability);
    if (key == null) return defaultEnabled;
    Map<String, Boolean> caps = readCapabilities();
    return caps.getOrDefault(key, defaultEnabled);
  }

  /**
   * Persists an IRCv3 capability request override under {@code ircafe.ui.ircv3Capabilities}.
   *
   * <p>Default behavior is "enabled", so enabled values are removed to keep YAML concise.
   */
  synchronized void rememberCapabilityEnabled(String capability, boolean enabled) {
    try {
      if (file.toString().isBlank()) return;

      String key = normalizeCapabilityKey(capability);
      if (key == null) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ui = getOrCreateMap(ircafe, "ui");
      Map<String, Object> caps = getOrCreateMap(ui, "ircv3Capabilities");

      if (enabled) {
        caps.remove(key);
      } else {
        caps.put(key, false);
      }
      if (caps.isEmpty()) {
        ui.remove("ircv3Capabilities");
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn(
          "[ircafe] Could not persist IRCv3 capability '{}' setting to '{}'", capability, file, e);
    }
  }

  private String normalizeCapabilityKey(String capability) {
    return capabilityNameResolver.normalizePreferenceKey(capability);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
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
}
