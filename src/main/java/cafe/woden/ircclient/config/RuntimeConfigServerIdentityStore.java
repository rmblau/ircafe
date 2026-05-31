package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigServerYamlSupport.findServerById;
import static cafe.woden.ircclient.config.RuntimeConfigServerYamlSupport.readServerList;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns per-server identity persistence in {@code irc.servers[]}. */
class RuntimeConfigServerIdentityStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigServerIdentityStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigServerIdentityStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberNick(String serverId, String nick) {
    updateServer(
        serverId,
        server -> {
          String n = Objects.toString(nick, "").trim();
          if (!n.isEmpty()) server.put("nick", n);
        });
  }

  private void updateServer(String serverId, ServerUpdater updater) {
    try {
      if (file.toString().isBlank()) return;
      String sid = Objects.toString(serverId, "").trim();
      if (sid.isEmpty()) return;

      Map<String, Object> doc = Files.exists(file) ? documentStore.load() : new LinkedHashMap<>();
      Map<String, Object> irc = getOrCreateMap(doc, "irc");
      List<Map<String, Object>> servers = readServerList(irc).orElseGet(ArrayList::new);

      Map<String, Object> found = findServerById(servers, sid).orElse(null);

      // Do not auto-create missing servers: removed servers must stay removed.
      if (found == null) return;

      updater.update(found);

      irc.put("servers", servers);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist server identity settings to '{}'", file, e);
    }
  }

  @FunctionalInterface
  private interface ServerUpdater {
    void update(Map<String, Object> serverMap);
  }
}
