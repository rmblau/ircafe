package cafe.woden.ircclient.config;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.layered.InfrastructureLayer;

/** Shared helpers for runtime configuration stored under {@code irc.servers[]}. */
@InfrastructureLayer
final class RuntimeConfigServerYamlSupport {

  private RuntimeConfigServerYamlSupport() {}

  @SuppressWarnings("unchecked")
  static Optional<List<Map<String, Object>>> readServerList(Map<String, Object> irc) {
    Object raw = irc.get("servers");
    if (raw instanceof List<?>) {
      return Optional.of((List<Map<String, Object>>) raw);
    }
    return Optional.empty();
  }

  static Optional<Map<String, Object>> findServerById(
      List<Map<String, Object>> servers, String serverId) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty() || servers == null || servers.isEmpty()) return Optional.empty();

    for (Map<String, Object> server : servers) {
      if (server == null) continue;
      if (sid.equalsIgnoreCase(Objects.toString(server.get("id"), "").trim())) {
        return Optional.of(server);
      }
    }
    return Optional.empty();
  }
}
