package cafe.woden.ircclient.config.runtime.server;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigServerYamlSection;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns per-server identity persistence in {@code irc.servers[]}. */
public class RuntimeConfigServerIdentityStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigServerIdentityStore.class);

  private final RuntimeConfigServerYamlSection servers;

  public RuntimeConfigServerIdentityStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.servers =
        new RuntimeConfigServerYamlSection(file, documentStore, log, "server identity settings");
  }

  public synchronized void rememberNick(String serverId, String nick) {
    updateServer(
        serverId,
        server -> {
          String n = Objects.toString(nick, "").trim();
          if (!n.isEmpty()) server.put("nick", n);
        });
  }

  private void updateServer(String serverId, Consumer<Map<String, Object>> updater) {
    servers.mutateExistingServer(serverId, updater);
  }
}
