package cafe.woden.ircclient.config.runtime.ircv3;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asInt;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asLong;

import cafe.woden.ircclient.config.api.Ircv3StsPolicyConfigPort;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted IRCv3 STS policy snapshots under {@code ircafe.ircv3.stsPolicies}. */
public class RuntimeConfigIrcv3StsPolicyStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigIrcv3StsPolicyStore.class);

  private final RuntimeConfigYamlSection ircv3Section;

  public RuntimeConfigIrcv3StsPolicyStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.ircv3Section = RuntimeConfigYamlSection.ircafe(file, documentStore, log, "ircv3");
  }

  /**
   * Reads persisted IRCv3 STS policy snapshots under {@code ircafe.ircv3.stsPolicies}.
   *
   * <p>Entries with invalid hosts or missing/invalid expiry are ignored.
   */
  public synchronized Map<String, Ircv3StsPolicyConfigPort.StsPolicySnapshot> readPolicies() {
    Optional<Object> policiesObj =
        ircv3Section.readExistingValue("IRCv3 STS policy cache", "stsPolicies");
    if (policiesObj.isEmpty()) return Map.of();
    if (!(policiesObj.get() instanceof Map<?, ?> policies)) return Map.of();

    Map<String, Ircv3StsPolicyConfigPort.StsPolicySnapshot> out = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : policies.entrySet()) {
      String host = normalizeHostKey(Objects.toString(entry.getKey(), ""));
      if (host == null) continue;
      if (!(entry.getValue() instanceof Map<?, ?> rawPolicy)) continue;

      long expiresAtEpochMs = asLong(rawPolicy.get("expiresAtEpochMs")).orElse(0L);
      if (expiresAtEpochMs <= 0L) continue;

      long durationSeconds = Math.max(0L, asLong(rawPolicy.get("durationSeconds")).orElse(0L));
      Integer port = asInt(rawPolicy.get("port")).orElse(null);
      if (port != null && (port <= 0 || port > 65_535)) {
        port = null;
      }
      boolean preload = asBoolean(rawPolicy.get("preload")).orElse(false);
      String rawValue = Objects.toString(rawPolicy.get("rawValue"), "").trim();

      out.put(
          host,
          new Ircv3StsPolicyConfigPort.StsPolicySnapshot(
              expiresAtEpochMs, port, preload, durationSeconds, rawValue));
    }
    return out;
  }

  /** Persists one IRCv3 STS policy snapshot under {@code ircafe.ircv3.stsPolicies.<host>}. */
  public synchronized void rememberPolicy(
      String host,
      long expiresAtEpochMs,
      Integer port,
      boolean preload,
      long durationSeconds,
      String rawValue) {
    String hostKey = normalizeHostKey(host);
    if (hostKey == null) return;
    if (expiresAtEpochMs <= 0L || durationSeconds <= 0L) {
      forgetPolicy(hostKey);
      return;
    }

    Integer normalizedPort = port;
    if (normalizedPort != null && (normalizedPort <= 0 || normalizedPort > 65_535)) {
      normalizedPort = null;
    }
    Integer persistedPort = normalizedPort;
    String normalizedRawValue = Objects.toString(rawValue, "").trim();

    ircv3Section.mutateMap(
        "IRCv3 STS policy for host '" + host + "'",
        policies -> {
          Map<String, Object> policy = new LinkedHashMap<>();
          policy.put("expiresAtEpochMs", expiresAtEpochMs);
          policy.put("durationSeconds", durationSeconds);
          if (persistedPort != null) {
            policy.put("port", persistedPort);
          }
          if (preload) {
            policy.put("preload", true);
          }
          if (!normalizedRawValue.isEmpty()) {
            policy.put("rawValue", normalizedRawValue);
          }

          policies.put(hostKey, policy);
        },
        "stsPolicies");
  }

  /** Removes a persisted IRCv3 STS policy snapshot from {@code ircafe.ircv3.stsPolicies}. */
  public synchronized void forgetPolicy(String host) {
    String hostKey = normalizeHostKey(host);
    if (hostKey == null) return;

    ircv3Section.mutateExistingMapAndRemoveIfEmpty(
        "IRCv3 STS policy removal for host '" + host + "'",
        policies -> {
          boolean removed = false;
          for (String k : new ArrayList<>(policies.keySet())) {
            if (hostKey.equalsIgnoreCase(Objects.toString(k, "").trim())) {
              policies.remove(k);
              removed = true;
            }
          }
          return removed;
        },
        "stsPolicies");
  }

  private static String normalizeHostKey(String host) {
    String h = Objects.toString(host, "").trim().toLowerCase(java.util.Locale.ROOT);
    return h.isEmpty() ? null : h;
  }
}
