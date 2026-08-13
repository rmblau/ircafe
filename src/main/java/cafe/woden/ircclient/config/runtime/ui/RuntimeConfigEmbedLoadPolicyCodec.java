package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asInt;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.sanitizeStringList;

import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicyScope;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Pure parsing and serialization policy for persisted embed/link loading settings. */
final class RuntimeConfigEmbedLoadPolicyCodec {

  private RuntimeConfigEmbedLoadPolicyCodec() {}

  static EmbedLoadPolicySnapshot parseSnapshot(Map<?, ?> policy) {
    EmbedLoadPolicyScope global = parseScope(policy.get("global"));

    LinkedHashMap<String, EmbedLoadPolicyScope> byServer = new LinkedHashMap<>();
    Object rawByServer = policy.get("byServer");
    if (rawByServer instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        String serverId = Objects.toString(entry.getKey(), "").trim();
        if (serverId.isEmpty() || !(entry.getValue() instanceof Map<?, ?>)) continue;
        EmbedLoadPolicyScope scope = parseScope(entry.getValue());
        if (!scope.isDefaultScope()) byServer.put(serverId, scope);
      }
    }

    return new EmbedLoadPolicySnapshot(global, byServer);
  }

  static Map<String, Object> serializeSnapshot(EmbedLoadPolicySnapshot snapshot) {
    EmbedLoadPolicySnapshot normalized =
        snapshot == null ? EmbedLoadPolicySnapshot.defaults() : snapshot;
    if (normalized.isDefaultPolicy()) return Map.of();

    Map<String, Object> policy = new LinkedHashMap<>();
    Map<String, Object> global = serializeScope(normalized.global());
    if (!global.isEmpty()) policy.put("global", global);

    Map<String, Object> byServer = new LinkedHashMap<>();
    if (normalized.byServer() != null) {
      for (Map.Entry<String, EmbedLoadPolicyScope> entry : normalized.byServer().entrySet()) {
        String serverId = Objects.toString(entry.getKey(), "").trim();
        Map<String, Object> scope = serializeScope(entry.getValue());
        if (!serverId.isEmpty() && !scope.isEmpty()) byServer.put(serverId, scope);
      }
    }
    if (!byServer.isEmpty()) policy.put("byServer", byServer);
    return Map.copyOf(policy);
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

  private static Map<String, Object> serializeScope(EmbedLoadPolicyScope scope) {
    Map<String, Object> out = new LinkedHashMap<>();
    EmbedLoadPolicyScope normalized = scope == null ? EmbedLoadPolicyScope.defaults() : scope;
    if (!normalized.userWhitelist().isEmpty()) out.put("userWhitelist", normalized.userWhitelist());
    if (!normalized.userBlacklist().isEmpty()) out.put("userBlacklist", normalized.userBlacklist());
    if (!normalized.channelWhitelist().isEmpty())
      out.put("channelWhitelist", normalized.channelWhitelist());
    if (!normalized.channelBlacklist().isEmpty())
      out.put("channelBlacklist", normalized.channelBlacklist());
    if (normalized.requireVoiceOrOp()) out.put("requireVoiceOrOp", true);
    if (normalized.requireLoggedIn()) out.put("requireLoggedIn", true);
    if (normalized.minAccountAgeDays() > 0)
      out.put("minAccountAgeDays", normalized.minAccountAgeDays());
    if (!normalized.linkWhitelist().isEmpty()) out.put("linkWhitelist", normalized.linkWhitelist());
    if (!normalized.linkBlacklist().isEmpty()) out.put("linkBlacklist", normalized.linkBlacklist());
    if (!normalized.domainWhitelist().isEmpty())
      out.put("domainWhitelist", normalized.domainWhitelist());
    if (!normalized.domainBlacklist().isEmpty())
      out.put("domainBlacklist", normalized.domainBlacklist());
    return out;
  }
}
