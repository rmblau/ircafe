package cafe.woden.ircclient.config.runtime.ircv3;

import cafe.woden.ircclient.config.api.Ircv3StsPolicyConfigPort;
import cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3StsPolicyCodec.StsPolicyPersistence;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
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
    return RuntimeConfigIrcv3StsPolicyCodec.parsePolicies(policies);
  }

  /** Persists one IRCv3 STS policy snapshot under {@code ircafe.ircv3.stsPolicies.<host>}. */
  public synchronized void rememberPolicy(
      String host,
      long expiresAtEpochMs,
      Integer port,
      boolean preload,
      long durationSeconds,
      String rawValue) {
    String hostKey = RuntimeConfigIrcv3StsPolicyCodec.normalizeHostKey(host);
    if (hostKey == null) return;
    StsPolicyPersistence policy =
        RuntimeConfigIrcv3StsPolicyCodec.normalizePolicy(
            expiresAtEpochMs, port, preload, durationSeconds, rawValue);
    if (policy == null) {
      forgetPolicy(hostKey);
      return;
    }

    ircv3Section.mutateMap(
        "IRCv3 STS policy for host '" + host + "'",
        policies -> policies.put(hostKey, policy.toYamlMap()),
        "stsPolicies");
  }

  /** Removes a persisted IRCv3 STS policy snapshot from {@code ircafe.ircv3.stsPolicies}. */
  public synchronized void forgetPolicy(String host) {
    String hostKey = RuntimeConfigIrcv3StsPolicyCodec.normalizeHostKey(host);
    if (hostKey == null) return;

    ircv3Section.mutateExistingMapAndRemoveIfEmpty(
        "IRCv3 STS policy removal for host '" + host + "'",
        policies -> {
          boolean removed = false;
          for (String k : new ArrayList<>(policies.keySet())) {
            if (RuntimeConfigIrcv3StsPolicyCodec.hostKeysMatch(hostKey, k)) {
              policies.remove(k);
              removed = true;
            }
          }
          return removed;
        },
        "stsPolicies");
  }
}
