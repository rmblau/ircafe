package cafe.woden.ircclient.config.runtime.server;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns startup auto-connect settings under {@code ircafe.ui}. */
public class RuntimeConfigServerAutoConnectStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigServerAutoConnectStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigServerAutoConnectStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "ui");
  }

  public synchronized void rememberAutoConnectOnStart(boolean enabled) {
    uiSection.putValue("autoConnectOnStart", enabled, "autoConnectOnStart");
  }

  /**
   * Reads persisted per-server startup auto-connect overrides.
   *
   * <p>Stored under {@code ircafe.ui.serverAutoConnectOnStartByServer.<serverId>}. Default behavior
   * is enabled, so this map usually contains only {@code false} entries.
   */
  public synchronized Map<String, Boolean> readServerAutoConnectOnStartByServer() {
    return uiSection
        .readExistingValue(
            "per-server startup auto-connect settings", "serverAutoConnectOnStartByServer")
        .map(RuntimeConfigServerAutoConnectStore::readBooleanMap)
        .orElse(Map.of());
  }

  /**
   * Reads whether a server should auto-connect on startup.
   *
   * <p>Returns {@code defaultValue} when no override is present.
   */
  public synchronized boolean readServerAutoConnectOnStart(String serverId, boolean defaultValue) {
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
  public synchronized void rememberServerAutoConnectOnStart(String serverId, boolean enabled) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return;

    uiSection.mutateMapAndRemoveIfEmpty(
        "per-server startup auto-connect settings",
        byServer -> {
          if (enabled) {
            byServer.remove(sid);
          } else {
            byServer.put(sid, false);
          }
        },
        "serverAutoConnectOnStartByServer");
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
