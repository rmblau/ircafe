package cafe.woden.ircclient.config.runtime.server;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Pure helpers for startup auto-connect settings keyed by server id. */
final class RuntimeConfigServerAutoConnectCodec {

  private RuntimeConfigServerAutoConnectCodec() {}

  static String normalizeServerId(Object serverId) {
    return Objects.toString(serverId, "").trim();
  }

  static Map<String, Boolean> readBooleanMap(Object raw) {
    if (!(raw instanceof Map<?, ?> byServer)) return Map.of();

    LinkedHashMap<String, Boolean> out = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : byServer.entrySet()) {
      String sid = normalizeServerId(entry.getKey());
      if (sid.isEmpty()) continue;
      Optional<Boolean> enabled = asBoolean(entry.getValue());
      enabled.ifPresent(value -> out.put(sid, value));
    }
    return out.isEmpty() ? Map.of() : Map.copyOf(out);
  }

  static boolean resolveServerAutoConnect(
      Map<String, Boolean> byServer, String serverId, boolean defaultValue) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return defaultValue;
    if (byServer == null || byServer.isEmpty()) return defaultValue;

    Boolean exact = byServer.get(sid);
    if (exact != null) return exact;

    for (Map.Entry<String, Boolean> entry : byServer.entrySet()) {
      if (sid.equalsIgnoreCase(normalizeServerId(entry.getKey()))) {
        return Boolean.TRUE.equals(entry.getValue());
      }
    }
    return defaultValue;
  }
}
