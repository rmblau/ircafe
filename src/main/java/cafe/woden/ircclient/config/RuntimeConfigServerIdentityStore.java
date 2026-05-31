package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigServerYamlSupport.mutateExistingServer;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigServerYamlSupport;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
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

  private void updateServer(String serverId, Consumer<Map<String, Object>> updater) {
    mutateExistingServer(
        file, documentStore, log, "server identity settings", serverId, updater);
  }
}
