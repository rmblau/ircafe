package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMapPath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns startup auto-connect settings under {@code ircafe.ui}. */
class RuntimeConfigServerAutoConnectStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigServerAutoConnectStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigServerAutoConnectStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberAutoConnectOnStart(boolean enabled) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = getOrCreateMapPath(doc, "ircafe", "ui");

      ui.put("autoConnectOnStart", enabled);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist autoConnectOnStart setting to '{}'", file, e);
    }
  }

  /**
   * Reads persisted per-server startup auto-connect overrides.
   *
   * <p>Stored under {@code ircafe.ui.serverAutoConnectOnStartByServer.<serverId>}. Default behavior
   * is enabled, so this map usually contains only {@code false} entries.
   */
  synchronized Map<String, Boolean> readServerAutoConnectOnStartByServer() {
    try {
      if (file.toString().isBlank()) return Map.of();
      if (!Files.exists(file)) return Map.of();

      Map<String, Object> doc = documentStore.load();
      Object byServerObj =
          RuntimeConfigDocumentPathReader.readValue(
                  doc, "ircafe", "ui", "serverAutoConnectOnStartByServer")
              .orElse(null);
      if (!(byServerObj instanceof Map<?, ?> byServer)) return Map.of();

      LinkedHashMap<String, Boolean> out = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : byServer.entrySet()) {
        String sid = Objects.toString(entry.getKey(), "").trim();
        if (sid.isEmpty()) continue;
        Optional<Boolean> enabled = asBoolean(entry.getValue());
        enabled.ifPresent(value -> out.put(sid, value));
      }
      if (out.isEmpty()) return Map.of();
      return Map.copyOf(out);
    } catch (Exception e) {
      log.warn(
          "[ircafe] Could not read per-server startup auto-connect settings from '{}'", file, e);
      return Map.of();
    }
  }

  /**
   * Reads whether a server should auto-connect on startup.
   *
   * <p>Returns {@code defaultValue} when no override is present.
   */
  synchronized boolean readServerAutoConnectOnStart(String serverId, boolean defaultValue) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return defaultValue;

    Map<String, Boolean> byServer = readServerAutoConnectOnStartByServer();
    Boolean exact = byServer.get(sid);
    if (exact != null) return exact;

    for (Map.Entry<String, Boolean> entry : byServer.entrySet()) {
      if (sid.equalsIgnoreCase(Objects.toString(entry.getKey(), "").trim())) {
        return Boolean.TRUE.equals(entry.getValue());
      }
    }
    return defaultValue;
  }

  /**
   * Persists whether a server should auto-connect on startup.
   *
   * <p>Enabled is the default, so enabled values are removed to keep the YAML concise.
   */
  synchronized void rememberServerAutoConnectOnStart(String serverId, boolean enabled) {
    try {
      if (file.toString().isBlank()) return;
      String sid = Objects.toString(serverId, "").trim();
      if (sid.isEmpty()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = getOrCreateMapPath(doc, "ircafe", "ui");
      Map<String, Object> byServer = getOrCreateMap(ui, "serverAutoConnectOnStartByServer");

      if (enabled) {
        byServer.remove(sid);
      } else {
        byServer.put(sid, false);
      }
      if (byServer.isEmpty()) {
        ui.remove("serverAutoConnectOnStartByServer");
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn(
          "[ircafe] Could not persist per-server startup auto-connect settings to '{}'", file, e);
    }
  }

}
