package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asInt;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asLong;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.mutateDocument;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.readExistingValue;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.readMap;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.removeIfEmpty;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.api.Ircv3StsPolicyConfigPort;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted IRCv3 STS policy snapshots under {@code ircafe.ircv3.stsPolicies}. */
class RuntimeConfigIrcv3StsPolicyStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigIrcv3StsPolicyStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigIrcv3StsPolicyStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  /**
   * Reads persisted IRCv3 STS policy snapshots under {@code ircafe.ircv3.stsPolicies}.
   *
   * <p>Entries with invalid hosts or missing/invalid expiry are ignored.
   */
  synchronized Map<String, Ircv3StsPolicyConfigPort.StsPolicySnapshot> readPolicies() {
    Optional<Object> policiesObj =
        readExistingValue(
            file,
            documentStore,
            log,
            "IRCv3 STS policy cache",
            "ircafe",
            "ircv3",
            "stsPolicies");
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
  synchronized void rememberPolicy(
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

    mutateDocument(
        file,
        documentStore,
        log,
        "IRCv3 STS policy for host '" + host + "'",
        doc -> {
          Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
          Map<String, Object> ircv3 = getOrCreateMap(ircafe, "ircv3");
          Map<String, Object> policies = getOrCreateMap(ircv3, "stsPolicies");

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
          return true;
        });
  }

  /** Removes a persisted IRCv3 STS policy snapshot from {@code ircafe.ircv3.stsPolicies}. */
  synchronized void forgetPolicy(String host) {
    String hostKey = normalizeHostKey(host);
    if (hostKey == null) return;

    mutateDocument(
        file,
        documentStore,
        log,
        "IRCv3 STS policy removal for host '" + host + "'",
        doc -> {
          Optional<Map<String, Object>> ircafeObj = readMap(doc, "ircafe");
          if (ircafeObj.isEmpty()) return false;
          Map<String, Object> ircafe = ircafeObj.get();

          Optional<Map<String, Object>> ircv3Obj = readMap(ircafe, "ircv3");
          if (ircv3Obj.isEmpty()) return false;
          Map<String, Object> ircv3 = ircv3Obj.get();

          Optional<Map<String, Object>> policiesObj = readMap(ircv3, "stsPolicies");
          if (policiesObj.isEmpty()) return false;
          Map<String, Object> policies = policiesObj.get();

          boolean removed = false;
          for (String k : new ArrayList<>(policies.keySet())) {
            if (hostKey.equalsIgnoreCase(Objects.toString(k, "").trim())) {
              policies.remove(k);
              removed = true;
            }
          }
          if (!removed) return false;

          removeIfEmpty(ircv3, "stsPolicies", policies);
          removeIfEmpty(ircafe, "ircv3", ircv3);
          removeIfEmpty(doc, "ircafe", ircafe);
          return true;
        });
  }

  private static String normalizeHostKey(String host) {
    String h = Objects.toString(host, "").trim().toLowerCase(java.util.Locale.ROOT);
    return h.isEmpty() ? null : h;
  }
}
