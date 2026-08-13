package cafe.woden.ircclient.config.runtime.server;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns the top-level {@code irc.servers} runtime-config document section.
 *
 * <p>The main {@link RuntimeConfigStore} still owns per-server runtime mutations such as joined
 * channels, monitor nicks, and server-tree state. This helper only covers the seed/write/read paths
 * for the server list itself.
 */
public class RuntimeConfigServerListStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigServerListStore.class);

  private final RuntimeConfigYamlSection ircSection;
  private final IrcProperties defaults;

  public RuntimeConfigServerListStore(
      Path file, RuntimeConfigDocumentStore documentStore, IrcProperties defaults) {
    this.ircSection = new RuntimeConfigYamlSection(file, documentStore, log, "irc");
    this.defaults = defaults;
  }

  public synchronized void ensureFileExistsWithServers() {
    ircSection.mutateMapIfChanged(
        "runtime config file",
        irc -> {
          // If the key exists, don't overwrite it. This is what makes removals stick.
          if (irc.containsKey("servers")) return false;

          irc.put(
              "servers",
              RuntimeConfigServerListCodec.serverMaps(
                  defaults == null ? null : defaults.servers()));
          return true;
        });
  }

  public synchronized void writeServers(List<IrcProperties.Server> servers) {
    ircSection.putValue(
        "servers list", RuntimeConfigServerListCodec.serverMaps(servers), "servers");
  }

  /** Returns configured server ids from runtime config, falling back to boot defaults. */
  public synchronized List<String> readServerIds() {
    Object raw = ircSection.readValue("server ids", "servers").orElse(null);
    return RuntimeConfigServerListCodec.readServerIds(raw, defaults);
  }

  /**
   * Returns runtime {@code autoJoin} entries for servers that explicitly define that key.
   *
   * <p>Only servers with an explicit {@code autoJoin} key are included. This allows callers to
   * treat runtime config as authoritative without conflating missing keys with inherited defaults.
   */
  public synchronized Map<String, List<String>> readExplicitServerAutoJoinById() {
    Object raw = ircSection.readExistingValue("explicit auto-join lists", "servers").orElse(null);
    return RuntimeConfigServerListCodec.readExplicitServerAutoJoinById(raw);
  }
}
