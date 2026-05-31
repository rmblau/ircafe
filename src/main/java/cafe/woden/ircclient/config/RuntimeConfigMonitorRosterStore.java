package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.containsIgnoreCase;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigServerYamlSection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns per-server IRCv3 MONITOR roster persistence under {@code irc.servers[].monitorNicks}. */
class RuntimeConfigMonitorRosterStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigMonitorRosterStore.class);

  private final RuntimeConfigServerYamlSection servers;

  RuntimeConfigMonitorRosterStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.servers =
        new RuntimeConfigServerYamlSection(file, documentStore, log, "monitor nick list");
  }

  synchronized void rememberMonitorNick(String serverId, String nick) {
    updateServer(
        serverId,
        server -> {
          String n = normalizeMonitorNick(nick);
          if (n.isEmpty()) return;

          List<String> monitorNicks = sanitizeMonitorNickList(server.get("monitorNicks"));
          if (containsIgnoreCase(monitorNicks, n)) return;
          monitorNicks.add(n);
          server.put("monitorNicks", monitorNicks);
        });
  }

  synchronized void forgetMonitorNick(String serverId, String nick) {
    updateServer(
        serverId,
        server -> {
          String n = normalizeMonitorNick(nick);
          if (n.isEmpty()) return;

          List<String> monitorNicks = sanitizeMonitorNickList(server.get("monitorNicks"));
          monitorNicks.removeIf(existing -> existing != null && existing.equalsIgnoreCase(n));
          if (monitorNicks.isEmpty()) {
            server.remove("monitorNicks");
          } else {
            server.put("monitorNicks", monitorNicks);
          }
        });
  }

  synchronized void replaceMonitorNicks(String serverId, List<String> nicks) {
    updateServer(
        serverId,
        server -> {
          List<String> monitorNicks = sanitizeMonitorNickList(nicks);
          if (monitorNicks.isEmpty()) {
            server.remove("monitorNicks");
          } else {
            server.put("monitorNicks", monitorNicks);
          }
        });
  }

  synchronized List<String> readMonitorNicks(String serverId) {
    return servers.readExistingServer(serverId)
        .map(server -> List.copyOf(sanitizeMonitorNickList(server.get("monitorNicks"))))
        .orElse(List.of());
  }

  private void updateServer(String serverId, Consumer<Map<String, Object>> updater) {
    servers.mutateExistingServer(serverId, updater);
  }

  private static List<String> sanitizeMonitorNickList(Object rawList) {
    if (!(rawList instanceof List<?> list) || list.isEmpty()) return new ArrayList<>();
    ArrayList<String> out = new ArrayList<>();
    for (Object raw : list) {
      String nick = normalizeMonitorNick(raw);
      if (nick.isEmpty()) continue;
      if (!containsIgnoreCase(out, nick)) out.add(nick);
    }
    if (out.isEmpty()) return new ArrayList<>();
    return out;
  }

  private static String normalizeMonitorNick(Object rawNick) {
    String nick = Objects.toString(rawNick, "").trim();
    if (nick.isEmpty()) return "";
    if (nick.startsWith(":")) nick = nick.substring(1).trim();
    int comma = nick.indexOf(',');
    if (comma >= 0) nick = nick.substring(0, comma).trim();
    int bang = nick.indexOf('!');
    if (bang > 0) nick = nick.substring(0, bang).trim();
    if (nick.isEmpty()) return "";
    if (nick.indexOf(' ') >= 0 || nick.indexOf('\t') >= 0) return "";
    if (nick.startsWith("#") || nick.startsWith("&")) return "";
    return nick;
  }
}
