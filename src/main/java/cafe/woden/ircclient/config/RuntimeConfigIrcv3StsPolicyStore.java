package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.asInt;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;

import cafe.woden.ircclient.config.api.Ircv3StsPolicyConfigPort;
import java.nio.file.Files;
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
    try {
      if (file.toString().isBlank()) return Map.of();
      if (!Files.exists(file)) return Map.of();

      Map<String, Object> doc = documentStore.load();
      Object ircafeObj = doc.get("ircafe");
      if (!(ircafeObj instanceof Map<?, ?> ircafe)) return Map.of();

      Object ircv3Obj = ircafe.get("ircv3");
      if (!(ircv3Obj instanceof Map<?, ?> ircv3)) return Map.of();

      Object policiesObj = ircv3.get("stsPolicies");
      if (!(policiesObj instanceof Map<?, ?> policies)) return Map.of();

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
    } catch (Exception e) {
      log.warn("[ircafe] Could not read IRCv3 STS policy cache from '{}'", file, e);
      return Map.of();
    }
  }

  /** Persists one IRCv3 STS policy snapshot under {@code ircafe.ircv3.stsPolicies.<host>}. */
  synchronized void rememberPolicy(
      String host,
      long expiresAtEpochMs,
      Integer port,
      boolean preload,
      long durationSeconds,
      String rawValue) {
    try {
      if (file.toString().isBlank()) return;

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

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ircv3 = getOrCreateMap(ircafe, "ircv3");
      Map<String, Object> policies = getOrCreateMap(ircv3, "stsPolicies");

      Map<String, Object> policy = new LinkedHashMap<>();
      policy.put("expiresAtEpochMs", expiresAtEpochMs);
      policy.put("durationSeconds", durationSeconds);
      if (normalizedPort != null) {
        policy.put("port", normalizedPort);
      }
      if (preload) {
        policy.put("preload", true);
      }
      String normalizedRawValue = Objects.toString(rawValue, "").trim();
      if (!normalizedRawValue.isEmpty()) {
        policy.put("rawValue", normalizedRawValue);
      }

      policies.put(hostKey, policy);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist IRCv3 STS policy for host '{}' to '{}'", host, file, e);
    }
  }

  /** Removes a persisted IRCv3 STS policy snapshot from {@code ircafe.ircv3.stsPolicies}. */
  synchronized void forgetPolicy(String host) {
    try {
      if (file.toString().isBlank()) return;

      String hostKey = normalizeHostKey(host);
      if (hostKey == null) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Object ircafeObj = doc.get("ircafe");
      if (!(ircafeObj instanceof Map<?, ?> ircafeRaw)) return;
      @SuppressWarnings("unchecked")
      Map<String, Object> ircafe = (Map<String, Object>) ircafeRaw;

      Object ircv3Obj = ircafe.get("ircv3");
      if (!(ircv3Obj instanceof Map<?, ?> ircv3Raw)) return;
      @SuppressWarnings("unchecked")
      Map<String, Object> ircv3 = (Map<String, Object>) ircv3Raw;

      Object policiesObj = ircv3.get("stsPolicies");
      if (!(policiesObj instanceof Map<?, ?> policiesRaw)) return;
      @SuppressWarnings("unchecked")
      Map<String, Object> policies = (Map<String, Object>) policiesRaw;

      boolean removed = false;
      for (String k : new ArrayList<>(policies.keySet())) {
        if (hostKey.equalsIgnoreCase(Objects.toString(k, "").trim())) {
          policies.remove(k);
          removed = true;
        }
      }
      if (!removed) return;

      if (policies.isEmpty()) {
        ircv3.remove("stsPolicies");
      }
      if (ircv3.isEmpty()) {
        ircafe.remove("ircv3");
      }
      if (ircafe.isEmpty()) {
        doc.remove("ircafe");
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not remove IRCv3 STS policy for host '{}' from '{}'", host, file, e);
    }
  }

  private static String normalizeHostKey(String host) {
    String h = Objects.toString(host, "").trim().toLowerCase(java.util.Locale.ROOT);
    return h.isEmpty() ? null : h;
  }

  private static Optional<Long> asLong(Object value) {
    if (value instanceof Number n) return Optional.of(n.longValue());
    if (value instanceof String s) {
      String t = s.trim();
      if (t.isEmpty()) return Optional.empty();
      try {
        return Optional.of(Long.parseLong(t));
      } catch (Exception ignored) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

}
