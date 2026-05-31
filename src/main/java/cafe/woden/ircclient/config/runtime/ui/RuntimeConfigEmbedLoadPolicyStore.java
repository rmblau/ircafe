package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asInt;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.sanitizeStringList;

import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicyScope;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns advanced embed/link loading policy settings under {@code ircafe.ui.embedLoadPolicy}. */
public class RuntimeConfigEmbedLoadPolicyStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigEmbedLoadPolicyStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigEmbedLoadPolicyStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized EmbedLoadPolicySnapshot read() {
    return uiSection
        .readValue("embed/link load policy", "embedLoadPolicy")
        .filter(Map.class::isInstance)
        .map(Map.class::cast)
        .map(RuntimeConfigEmbedLoadPolicyStore::parseSnapshot)
        .orElseGet(EmbedLoadPolicySnapshot::defaults);
  }

  public synchronized void remember(EmbedLoadPolicySnapshot snapshot) {
    EmbedLoadPolicySnapshot normalized =
        snapshot == null ? EmbedLoadPolicySnapshot.defaults() : snapshot;

    uiSection.mutateMap("embed/link load policy", ui -> writePolicy(ui, normalized));
  }

  private static EmbedLoadPolicySnapshot parseSnapshot(Map<?, ?> policy) {
    EmbedLoadPolicyScope global = parseScope(policy.get("global"));

    LinkedHashMap<String, EmbedLoadPolicyScope> byServer = new LinkedHashMap<>();
    Object rawByServer = policy.get("byServer");
    if (rawByServer instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        String serverId = Objects.toString(entry.getKey(), "").trim();
        if (serverId.isEmpty()) continue;
        EmbedLoadPolicyScope scope = parseScope(entry.getValue());
        if (scope.isDefaultScope()) continue;
        byServer.put(serverId, scope);
      }
    }

    return new EmbedLoadPolicySnapshot(global, byServer);
  }

  private static void writePolicy(Map<String, Object> ui, EmbedLoadPolicySnapshot normalized) {
    if (normalized.isDefaultPolicy()) {
      ui.remove("embedLoadPolicy");
      return;
    }

    Map<String, Object> policy = getOrCreateMap(ui, "embedLoadPolicy");
    Map<String, Object> global = getOrCreateMap(policy, "global");
    writeScopeMap(global, normalized.global());

    if (normalized.byServer() == null || normalized.byServer().isEmpty()) {
      policy.remove("byServer");
    } else {
      Map<String, Object> byServer = getOrCreateMap(policy, "byServer");
      byServer.clear();
      for (Map.Entry<String, EmbedLoadPolicyScope> entry : normalized.byServer().entrySet()) {
        String serverId = Objects.toString(entry.getKey(), "").trim();
        if (serverId.isEmpty()) continue;
        EmbedLoadPolicyScope scope =
            entry.getValue() == null ? EmbedLoadPolicyScope.defaults() : entry.getValue();
        if (scope.isDefaultScope()) continue;
        Map<String, Object> scopeMap = new LinkedHashMap<>();
        writeScopeMap(scopeMap, scope);
        byServer.put(serverId, scopeMap);
      }
      if (byServer.isEmpty()) {
        policy.remove("byServer");
      }
    }

    if (policy.isEmpty()) {
      ui.remove("embedLoadPolicy");
    }
  }

  private static EmbedLoadPolicyScope parseScope(Object raw) {
    if (!(raw instanceof Map<?, ?> scope)) return EmbedLoadPolicyScope.defaults();
    return new EmbedLoadPolicyScope(
        sanitizeStringList(scope.get("userWhitelist")),
        sanitizeStringList(scope.get("userBlacklist")),
        sanitizeStringList(scope.get("channelWhitelist")),
        sanitizeStringList(scope.get("channelBlacklist")),
        asBoolean(scope.get("requireVoiceOrOp")).orElse(Boolean.FALSE),
        asBoolean(scope.get("requireLoggedIn")).orElse(Boolean.FALSE),
        Math.max(0, asInt(scope.get("minAccountAgeDays")).orElse(0)),
        sanitizeStringList(scope.get("linkWhitelist")),
        sanitizeStringList(scope.get("linkBlacklist")),
        sanitizeStringList(scope.get("domainWhitelist")),
        sanitizeStringList(scope.get("domainBlacklist")));
  }

  private static void writeScopeMap(Map<String, Object> out, EmbedLoadPolicyScope scope) {
    if (out == null) return;
    out.clear();
    EmbedLoadPolicyScope s = (scope == null) ? EmbedLoadPolicyScope.defaults() : scope;
    if (!s.userWhitelist().isEmpty()) out.put("userWhitelist", s.userWhitelist());
    if (!s.userBlacklist().isEmpty()) out.put("userBlacklist", s.userBlacklist());
    if (!s.channelWhitelist().isEmpty()) out.put("channelWhitelist", s.channelWhitelist());
    if (!s.channelBlacklist().isEmpty()) out.put("channelBlacklist", s.channelBlacklist());
    if (s.requireVoiceOrOp()) out.put("requireVoiceOrOp", true);
    if (s.requireLoggedIn()) out.put("requireLoggedIn", true);
    if (s.minAccountAgeDays() > 0) out.put("minAccountAgeDays", s.minAccountAgeDays());
    if (!s.linkWhitelist().isEmpty()) out.put("linkWhitelist", s.linkWhitelist());
    if (!s.linkBlacklist().isEmpty()) out.put("linkBlacklist", s.linkBlacklist());
    if (!s.domainWhitelist().isEmpty()) out.put("domainWhitelist", s.domainWhitelist());
    if (!s.domainBlacklist().isEmpty()) out.put("domainBlacklist", s.domainBlacklist());
  }

}
