package cafe.woden.ircclient.config;

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

/** Owns per-server private-message target persistence in {@code irc.servers[].autoJoin}. */
class RuntimeConfigPrivateMessageTargetStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigPrivateMessageTargetStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigPrivateMessageTargetStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberPrivateMessageTarget(String serverId, String nick) {
    updateServer(
        serverId,
        server -> {
          String n = Objects.toString(nick, "").trim();
          if (n.isEmpty()) return;

          List<String> autoJoin = getOrCreateStringList(server, "autoJoin");
          if (AutoJoinEntryCodec.privateMessageNicks(autoJoin).stream()
              .anyMatch(existing -> existing.equalsIgnoreCase(n))) {
            return;
          }
          String encoded = AutoJoinEntryCodec.encodePrivateMessageNick(n);
          if (!encoded.isEmpty()) {
            autoJoin.add(encoded);
          }
        });
  }

  synchronized void forgetPrivateMessageTarget(String serverId, String nick) {
    updateServer(
        serverId,
        server -> {
          String n = Objects.toString(nick, "").trim();
          if (n.isEmpty()) return;

          Object o = server.get("autoJoin");
          if (!(o instanceof List<?> list)) return;
          @SuppressWarnings("unchecked")
          List<String> autoJoin = (List<String>) list;
          autoJoin.removeIf(
              entry -> {
                String decoded = AutoJoinEntryCodec.decodePrivateMessageNick(entry);
                return !decoded.isEmpty() && decoded.equalsIgnoreCase(n);
              });
        });
  }

  synchronized List<String> readPrivateMessageTargets(String serverId) {
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
        Object autoJoinObj = server.get("autoJoin");
        if (!(autoJoinObj instanceof List<?> rawList)) return List.of();
        @SuppressWarnings("unchecked")
        List<String> autoJoin = (List<String>) rawList;
        return List.copyOf(AutoJoinEntryCodec.privateMessageNicks(autoJoin));
      }
    } catch (Exception e) {
      log.warn("[ircafe] Could not read private-message target list from '{}'", file, e);
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
      log.warn("[ircafe] Could not persist private-message target list to '{}'", file, e);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }

  @SuppressWarnings("unchecked")
  private static Optional<List<Map<String, Object>>> readServerList(Map<String, Object> irc) {
    Object o = irc.get("servers");
    if (o instanceof List<?>) {
      return Optional.of((List<Map<String, Object>>) o);
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  private static List<String> getOrCreateStringList(Map<String, Object> m, String key) {
    Object o = m.get(key);
    if (o instanceof List<?>) {
      return (List<String>) o;
    }
    List<String> created = new ArrayList<>();
    m.put(key, created);
    return created;
  }

  @FunctionalInterface
  private interface ServerUpdater {
    void update(Map<String, Object> serverMap);
  }
}
