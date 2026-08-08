package cafe.woden.ircclient.config.runtime.server;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.containsIgnoreCase;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigServerYamlSection;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns per-server IRCv3 MONITOR roster persistence under {@code irc.servers[].monitorNicks}. */
public class RuntimeConfigMonitorRosterStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigMonitorRosterStore.class);

  private final RuntimeConfigServerYamlSection servers;

  public RuntimeConfigMonitorRosterStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.servers =
        new RuntimeConfigServerYamlSection(file, documentStore, log, "monitor nick list");
  }

  public synchronized void rememberMonitorNick(String serverId, String nick) {
    updateServer(
        serverId,
        server -> {
          String n = RuntimeConfigMonitorRosterCodec.normalizeMonitorNick(nick);
          if (n.isEmpty()) return;

          List<String> monitorNicks =
              RuntimeConfigMonitorRosterCodec.sanitizeMonitorNickList(server.get("monitorNicks"));
          if (containsIgnoreCase(monitorNicks, n)) return;
          monitorNicks.add(n);
          server.put("monitorNicks", monitorNicks);
        });
  }

  public synchronized void forgetMonitorNick(String serverId, String nick) {
    updateServer(
        serverId,
        server -> {
          String n = RuntimeConfigMonitorRosterCodec.normalizeMonitorNick(nick);
          if (n.isEmpty()) return;

          List<String> monitorNicks =
              RuntimeConfigMonitorRosterCodec.sanitizeMonitorNickList(server.get("monitorNicks"));
          monitorNicks.removeIf(existing -> existing != null && existing.equalsIgnoreCase(n));
          if (monitorNicks.isEmpty()) {
            server.remove("monitorNicks");
          } else {
            server.put("monitorNicks", monitorNicks);
          }
        });
  }

  public synchronized void replaceMonitorNicks(String serverId, List<String> nicks) {
    updateServer(
        serverId,
        server -> {
          List<String> monitorNicks =
              RuntimeConfigMonitorRosterCodec.sanitizeMonitorNickList(nicks);
          if (monitorNicks.isEmpty()) {
            server.remove("monitorNicks");
          } else {
            server.put("monitorNicks", monitorNicks);
          }
        });
  }

  public synchronized List<String> readMonitorNicks(String serverId) {
    return servers
        .readExistingServer(serverId)
        .map(
            server ->
                List.copyOf(
                    RuntimeConfigMonitorRosterCodec.sanitizeMonitorNickList(
                        server.get("monitorNicks"))))
        .orElse(List.of());
  }

  private void updateServer(String serverId, Consumer<Map<String, Object>> updater) {
    servers.mutateExistingServer(serverId, updater);
  }
}
