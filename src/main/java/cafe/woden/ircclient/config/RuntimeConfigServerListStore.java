package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.containsIgnoreCase;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.sanitizeStringList;

import cafe.woden.ircclient.config.api.BackendDescriptorCatalog;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns the top-level {@code irc.servers} runtime-config document section.
 *
 * <p>The main {@link RuntimeConfigStore} still owns per-server runtime mutations such as joined
 * channels, monitor nicks, and server-tree state. This helper only covers the seed/write/read paths
 * for the server list itself.
 */
class RuntimeConfigServerListStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigServerListStore.class);

  private final RuntimeConfigYamlSection ircSection;
  private final IrcProperties defaults;

  RuntimeConfigServerListStore(
      Path file, RuntimeConfigDocumentStore documentStore, IrcProperties defaults) {
    this.ircSection = new RuntimeConfigYamlSection(file, documentStore, log, "irc");
    this.defaults = defaults;
  }

  synchronized void ensureFileExistsWithServers() {
    ircSection.mutateMapIfChanged(
        "runtime config file",
        irc -> {
          // If the key exists, don't overwrite it. This is what makes removals stick.
          if (irc.containsKey("servers")) return false;

          irc.put("servers", serverMaps(defaults == null ? null : defaults.servers()));
          return true;
        });
  }

  synchronized void writeServers(List<IrcProperties.Server> servers) {
    ircSection.putValue("servers list", serverMaps(servers), "servers");
  }

  /** Returns configured server ids from runtime config, falling back to boot defaults. */
  synchronized List<String> readServerIds() {
    Object raw = ircSection.readValue("server ids", "servers").orElse(null);
    if (!(raw instanceof List<?> servers) || servers.isEmpty()) return defaultServerIds();

    ArrayList<String> out = new ArrayList<>();
    for (Object item : servers) {
      if (!(item instanceof Map<?, ?> server)) continue;
      String id = Objects.toString(server.get("id"), "").trim();
      if (id.isEmpty()) continue;
      if (containsIgnoreCase(out, id)) continue;
      out.add(id);
    }
    if (out.isEmpty()) return defaultServerIds();
    return List.copyOf(out);
  }

  /**
   * Returns runtime {@code autoJoin} entries for servers that explicitly define that key.
   *
   * <p>Only servers with an explicit {@code autoJoin} key are included. This allows callers to
   * treat runtime config as authoritative without conflating missing keys with inherited defaults.
   */
  synchronized Map<String, List<String>> readExplicitServerAutoJoinById() {
    Object raw = ircSection.readExistingValue("explicit auto-join lists", "servers").orElse(null);
    if (!(raw instanceof List<?> servers) || servers.isEmpty()) return Map.of();

    LinkedHashMap<String, List<String>> out = new LinkedHashMap<>();
    for (Object item : servers) {
      if (!(item instanceof Map<?, ?> server)) continue;
      String id = Objects.toString(server.get("id"), "").trim();
      if (id.isEmpty()) continue;
      if (!server.containsKey("autoJoin")) continue;
      out.put(id, sanitizeStringList(server.get("autoJoin")));
    }
    return out.isEmpty() ? Map.of() : Map.copyOf(out);
  }

  private List<String> defaultServerIds() {
    if (defaults == null || defaults.servers() == null || defaults.servers().isEmpty()) {
      return List.of();
    }
    ArrayList<String> ids = new ArrayList<>();
    for (IrcProperties.Server s : defaults.servers()) {
      if (s == null || s.id() == null || s.id().isBlank()) continue;
      if (containsIgnoreCase(ids, s.id())) continue;
      ids.add(s.id().trim());
    }
    return List.copyOf(ids);
  }

  private static List<Map<String, Object>> serverMaps(List<IrcProperties.Server> servers) {
    ArrayList<Map<String, Object>> out = new ArrayList<>();
    if (servers == null) return out;

    for (IrcProperties.Server server : servers) {
      if (server == null) continue;
      out.add(toServerMap(server));
    }
    return out;
  }

  private static Map<String, Object> toServerMap(IrcProperties.Server s) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", s.id());
    m.put("host", s.host());
    m.put("port", s.port());
    m.put("tls", s.tls());
    String backendId = BackendDescriptorCatalog.builtIns().normalizeIdOrDefault(s.backendId());
    if (!backendId.equals(
        BackendDescriptorCatalog.builtIns().idFor(IrcProperties.Server.Backend.IRC))) {
      m.put("backend", backendId);
    }
    if (s.serverPassword() != null && !s.serverPassword().isBlank()) {
      m.put("serverPassword", s.serverPassword());
    }
    if (s.nick() != null) m.put("nick", s.nick());
    if (s.login() != null && !s.login().isBlank()) m.put("login", s.login());
    if (s.realName() != null && !s.realName().isBlank()) m.put("realName", s.realName());
    if (s.autoJoin() != null && !s.autoJoin().isEmpty()) {
      m.put("autoJoin", new ArrayList<>(s.autoJoin()));
    }
    if (s.perform() != null && !s.perform().isEmpty()) {
      m.put("perform", new ArrayList<>(s.perform()));
    }
    if (s.sasl() != null && s.sasl().enabled()) {
      Map<String, Object> sasl = new LinkedHashMap<>();
      sasl.put("enabled", true);
      sasl.put("username", s.sasl().username());
      sasl.put("password", s.sasl().password());
      if (s.sasl().mechanism() != null && !s.sasl().mechanism().isBlank()) {
        sasl.put("mechanism", s.sasl().mechanism());
      }
      // Persist only when diverging from the default strict behavior.
      // Default (when omitted) is: disconnectOnFailure = true.
      if (s.sasl().disconnectOnFailure() != null && !s.sasl().disconnectOnFailure()) {
        sasl.put("disconnectOnFailure", false);
      }
      m.put("sasl", sasl);
    }
    if (s.nickserv() != null && s.nickserv().enabled()) {
      Map<String, Object> nickserv = new LinkedHashMap<>();
      nickserv.put("enabled", true);
      nickserv.put("password", s.nickserv().password());
      if (s.nickserv().service() != null
          && !s.nickserv().service().isBlank()
          && !"NickServ".equalsIgnoreCase(s.nickserv().service().trim())) {
        nickserv.put("service", s.nickserv().service());
      }
      if (s.nickserv().delayJoinUntilIdentified() != null
          && !s.nickserv().delayJoinUntilIdentified()) {
        nickserv.put("delayJoinUntilIdentified", false);
      }
      m.put("nickserv", nickserv);
    }

    // Optional per-server SOCKS5 proxy override.
    // If present with enabled=false, this represents an explicit "disable proxy" for this server.
    if (s.proxy() != null) {
      IrcProperties.Proxy p = s.proxy();
      Map<String, Object> proxy = new LinkedHashMap<>();
      proxy.put("enabled", p.enabled());
      proxy.put("host", Objects.toString(p.host(), "").trim());
      proxy.put("port", Math.max(0, p.port()));
      proxy.put("username", Objects.toString(p.username(), "").trim());
      proxy.put("password", Objects.toString(p.password(), ""));
      proxy.put("remoteDns", p.remoteDns());
      proxy.put("connectTimeoutMs", Math.max(0L, p.connectTimeoutMs()));
      proxy.put("readTimeoutMs", Math.max(0L, p.readTimeoutMs()));
      m.put("proxy", proxy);
    }
    return m;
  }
}
