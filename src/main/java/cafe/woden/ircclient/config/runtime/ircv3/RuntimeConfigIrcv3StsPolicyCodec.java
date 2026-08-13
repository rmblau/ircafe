package cafe.woden.ircclient.config.runtime.ircv3;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asInt;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asLong;

import cafe.woden.ircclient.config.api.Ircv3StsPolicyConfigPort;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Pure codec/policy helpers for persisted IRCv3 STS policy snapshots. */
final class RuntimeConfigIrcv3StsPolicyCodec {

  private RuntimeConfigIrcv3StsPolicyCodec() {}

  static Map<String, Ircv3StsPolicyConfigPort.StsPolicySnapshot> parsePolicies(
      Map<?, ?> rawPolicies) {
    if (rawPolicies == null || rawPolicies.isEmpty()) return Map.of();

    Map<String, Ircv3StsPolicyConfigPort.StsPolicySnapshot> out = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : rawPolicies.entrySet()) {
      String host = normalizeHostKey(entry.getKey());
      if (host == null) continue;
      StsPolicyPersistence persistence = parsePolicy(entry.getValue());
      if (persistence == null) continue;
      out.put(host, persistence.toSnapshot());
    }
    return out;
  }

  static StsPolicyPersistence normalizePolicy(
      long expiresAtEpochMs, Integer port, boolean preload, long durationSeconds, String rawValue) {
    if (expiresAtEpochMs <= 0L || durationSeconds <= 0L) {
      return null;
    }
    return new StsPolicyPersistence(
        expiresAtEpochMs, normalizePort(port), preload, durationSeconds, normalizeString(rawValue));
  }

  static String normalizeHostKey(Object host) {
    String normalized = normalizeString(host).toLowerCase(Locale.ROOT);
    return normalized.isEmpty() ? null : normalized;
  }

  static boolean hostKeysMatch(Object left, Object right) {
    String leftKey = normalizeHostKey(left);
    String rightKey = normalizeHostKey(right);
    return leftKey != null && leftKey.equals(rightKey);
  }

  private static StsPolicyPersistence parsePolicy(Object raw) {
    if (!(raw instanceof Map<?, ?> rawPolicy)) return null;

    long expiresAtEpochMs = asLong(rawPolicy.get("expiresAtEpochMs")).orElse(0L);
    if (expiresAtEpochMs <= 0L) return null;

    long durationSeconds = Math.max(0L, asLong(rawPolicy.get("durationSeconds")).orElse(0L));
    Integer port = normalizePort(asInt(rawPolicy.get("port")).orElse(null));
    boolean preload = asBoolean(rawPolicy.get("preload")).orElse(false);
    String rawValue = normalizeString(rawPolicy.get("rawValue"));
    return new StsPolicyPersistence(expiresAtEpochMs, port, preload, durationSeconds, rawValue);
  }

  private static Integer normalizePort(Integer port) {
    if (port != null && (port <= 0 || port > 65_535)) {
      return null;
    }
    return port;
  }

  private static String normalizeString(Object value) {
    return Objects.toString(value, "").trim();
  }

  record StsPolicyPersistence(
      long expiresAtEpochMs, Integer port, boolean preload, long durationSeconds, String rawValue) {

    Ircv3StsPolicyConfigPort.StsPolicySnapshot toSnapshot() {
      return new Ircv3StsPolicyConfigPort.StsPolicySnapshot(
          expiresAtEpochMs, port, preload, durationSeconds, rawValue);
    }

    Map<String, Object> toYamlMap() {
      Map<String, Object> policy = new LinkedHashMap<>();
      policy.put("expiresAtEpochMs", expiresAtEpochMs);
      policy.put("durationSeconds", durationSeconds);
      if (port != null) {
        policy.put("port", port);
      }
      if (preload) {
        policy.put("preload", true);
      }
      if (!rawValue.isEmpty()) {
        policy.put("rawValue", rawValue);
      }
      return policy;
    }
  }
}
