package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.mutateMap;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.putValue;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.readExistingValue;

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
    putValue(
        file,
        documentStore,
        log,
        "autoConnectOnStart",
        enabled,
        "ircafe",
        "ui",
        "autoConnectOnStart");
  }

  /**
   * Reads persisted per-server startup auto-connect overrides.
   *
   * <p>Stored under {@code ircafe.ui.serverAutoConnectOnStartByServer.<serverId>}. Default behavior
   * is enabled, so this map usually contains only {@code false} entries.
   */
  synchronized Map<String, Boolean> readServerAutoConnectOnStartByServer() {
    return readExistingValue(
            file,
            documentStore,
            log,
            "per-server startup auto-connect settings",
            "ircafe",
            "ui",
            "serverAutoConnectOnStartByServer")
        .map(RuntimeConfigServerAutoConnectStore::readBooleanMap)
        .orElse(Map.of());
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
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return;

    mutateMap(
        file,
        documentStore,
        log,
        "per-server startup auto-connect settings",
        ui -> {
          Map<String, Object> byServer = getOrCreateMap(ui, "serverAutoConnectOnStartByServer");
          if (enabled) {
            byServer.remove(sid);
          } else {
            byServer.put(sid, false);
          }
          if (byServer.isEmpty()) {
            ui.remove("serverAutoConnectOnStartByServer");
          }
        },
        "ircafe",
        "ui");
  }

  private static Map<String, Boolean> readBooleanMap(Object raw) {
    if (!(raw instanceof Map<?, ?> byServer)) return Map.of();

    LinkedHashMap<String, Boolean> out = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : byServer.entrySet()) {
      String sid = Objects.toString(entry.getKey(), "").trim();
      if (sid.isEmpty()) continue;
      Optional<Boolean> enabled = asBoolean(entry.getValue());
      enabled.ifPresent(value -> out.put(sid, value));
    }
    return out.isEmpty() ? Map.of() : Map.copyOf(out);
  }

}
