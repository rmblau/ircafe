package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigServerYamlSupport.mutateExistingServer;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigServerYamlSupport.readExistingServer;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateStringList;

import cafe.woden.ircclient.config.api.AutoJoinEntryCodec;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigServerYamlSupport;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
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
    return readExistingServer(
            file, documentStore, log, "private-message target list", serverId)
        .map(RuntimeConfigPrivateMessageTargetStore::readPrivateMessageTargets)
        .orElse(List.of());
  }

  private void updateServer(String serverId, Consumer<Map<String, Object>> updater) {
    mutateExistingServer(
        file, documentStore, log, "private-message target list", serverId, updater);
  }

  @SuppressWarnings("unchecked")
  private static List<String> readPrivateMessageTargets(Map<String, Object> server) {
    Object autoJoinObj = server.get("autoJoin");
    if (!(autoJoinObj instanceof List<?> rawList)) return List.of();
    return List.copyOf(AutoJoinEntryCodec.privateMessageNicks((List<String>) rawList));
  }
}
