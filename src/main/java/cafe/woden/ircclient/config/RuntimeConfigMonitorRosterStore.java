package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns per-server IRCv3 MONITOR roster persistence under {@code irc.servers[].monitorNicks}. */
class RuntimeConfigMonitorRosterStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigMonitorRosterStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigMonitorRosterStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
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
    try {
      if (file.toString().isBlank()) return List.of();
      String sid = Objects.toString(serverId, "").trim();
      if (sid.isEmpty()) return List.of();

      Map<String, Object> doc = Files.exists(file) ? documentStore.load() : new LinkedHashMap<>();
      Map<String, Object> irc = getOrCreateMap(doc, "irc");
      List<Map<String, Object>> servers = readServerList(irc).orElse(List.of());
      for (Map<String, Object> server : servers) {
        if (server == null) continue;
        if (!sid.equalsIgnoreCase(Objects.toString(server.get("id"), "").trim())) continue;
        return List.copyOf(sanitizeMonitorNickList(server.get("monitorNicks")));
      }
    } catch (Exception e) {
      log.warn("[ircafe] Could not read monitor nick list from '{}'", file, e);
    }
    return List.of();
  }

  private void updateServer(String serverId, ServerUpdater updater) {
    try {
      if (file.toString().isBlank()) return;
      String sid = Objects.toString(serverId, "").trim();
      if (sid.isEmpty()) return;

      Map<String, Object> doc = Files.exists(file) ? documentStore.load() : new LinkedHashMap<>();
      Map<String, Object> irc = getOrCreateMap(doc, "irc");
      List<Map<String, Object>> servers = readServerList(irc).orElseGet(ArrayList::new);

      Map<String, Object> found = null;
      for (Map<String, Object> server : servers) {
        if (sid.equalsIgnoreCase(Objects.toString(server.get("id"), "").trim())) {
          found = server;
          break;
        }
      }

      // Do not auto-create missing servers: removed servers must stay removed.
      if (found == null) return;

      updater.update(found);
      irc.put("servers", servers);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist monitor nick list to '{}'", file, e);
    }
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

  private static boolean containsIgnoreCase(List<String> values, String needle) {
    if (values == null || values.isEmpty()) return false;
    String n = Objects.toString(needle, "").trim();
    if (n.isEmpty()) return false;
    for (String value : values) {
      if (value != null && value.equalsIgnoreCase(n)) return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private static Optional<List<Map<String, Object>>> readServerList(Map<String, Object> irc) {
    Object o = irc.get("servers");
    if (o instanceof List<?>) {
      return Optional.of((List<Map<String, Object>>) o);
    }
    return Optional.empty();
  }

  @FunctionalInterface
  private interface ServerUpdater {
    void update(Map<String, Object> serverMap);
  }
}
