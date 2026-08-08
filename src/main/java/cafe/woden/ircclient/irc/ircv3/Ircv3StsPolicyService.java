package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.Ircv3StsPolicyConfigPort;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Root persistence/cache adapter for feature-owned IRCv3 STS lifecycle policy.
 *
 * <p>The focused STS feature owns learning, expiration, persisted snapshot normalization, and
 * transport-upgrade decisions. This component owns concurrent cache mutation, runtime-config I/O,
 * logging, and adaptation back to {@link IrcProperties.Server}.
 */
@Component
@InfrastructureLayer
public class Ircv3StsPolicyService {

  private static final Logger log = LoggerFactory.getLogger(Ircv3StsPolicyService.class);

  private final ConcurrentMap<String, Ircv3StsPolicy> byHostLower = new ConcurrentHashMap<>();
  private final Ircv3StsPolicyConfigPort runtimeConfig;
  private final Ircv3StsRuntimeSupport runtimeSupport;
  private final Ircv3StsPersistedPolicyNormalizer persistedPolicyNormalizer =
      new Ircv3StsPersistedPolicyNormalizer();
  private final Ircv3StsTransportUpgradePlanner transportUpgradePlanner =
      new Ircv3StsTransportUpgradePlanner();

  @Autowired
  public Ircv3StsPolicyService(
      Ircv3StsPolicyConfigPort runtimeConfig,
      Ircv3InboundCommandSignalRuntimeCatalog inboundCommandRuntimeCatalog) {
    this.runtimeConfig = runtimeConfig;
    this.runtimeSupport = new Ircv3StsRuntimeSupport(inboundCommandRuntimeCatalog);
    loadPersistedPolicies();
  }

  public IrcProperties.Server applyPolicy(IrcProperties.Server configured) {
    if (configured == null) return null;
    String hostLower = Ircv3StsPolicy.normalizeHost(configured.host());
    if (hostLower.isEmpty()) return configured;

    Ircv3StsPolicy policy = activePolicyForHost(hostLower).orElse(null);
    if (policy == null) return configured;

    Ircv3StsTransportUpgradePlanner.Plan plan =
        transportUpgradePlanner.plan(policy, configured.port(), configured.tls());
    if (!plan.changed()) return configured;

    log.info(
        "[{}] applying STS policy for host={} (tls={}=>{}, port={}=>{}, preload={}, expiresAt={})",
        configured.id(),
        configured.host(),
        configured.tls(),
        plan.tls(),
        configured.port(),
        plan.port(),
        policy.preload(),
        policy.expiresAtEpochMs());

    return configured.withTransport(plan.port(), plan.tls());
  }

  public void observeFromCapList(
      String serverId, String host, boolean secureConnection, String capListRaw) {
    long observedAtEpochMilli = System.currentTimeMillis();
    for (Ircv3StsPolicyLearningPlanner.Decision decision :
        runtimeSupport.observe(host, secureConnection, capListRaw, observedAtEpochMilli)) {
      applyLearningDecision(serverId, decision);
    }
  }

  public Optional<Ircv3StsPolicy> activePolicyForHost(String host) {
    String hostLower = Ircv3StsPolicy.normalizeHost(host);
    if (hostLower.isEmpty()) return Optional.empty();

    Ircv3StsPolicy policy = byHostLower.get(hostLower);
    if (policy == null) return Optional.empty();

    if (policy.isExpired(System.currentTimeMillis())) {
      if (byHostLower.remove(hostLower, policy)) {
        forgetPersistedPolicy(hostLower);
      }
      return Optional.empty();
    }
    return Optional.of(policy);
  }

  private void loadPersistedPolicies() {
    Ircv3StsPolicyConfigPort store = runtimeConfig;
    if (store == null) return;

    Map<String, Ircv3StsPolicyConfigPort.StsPolicySnapshot> persisted =
        store.readIrcv3StsPolicies();
    if (persisted == null || persisted.isEmpty()) return;

    long now = System.currentTimeMillis();
    int loaded = 0;
    int dropped = 0;
    for (Map.Entry<String, Ircv3StsPolicyConfigPort.StsPolicySnapshot> entry :
        persisted.entrySet()) {
      Ircv3StsPolicyConfigPort.StsPolicySnapshot snapshot = entry.getValue();
      Ircv3StsPersistedPolicyNormalizer.Snapshot featureSnapshot =
          snapshot == null
              ? null
              : new Ircv3StsPersistedPolicyNormalizer.Snapshot(
                  snapshot.expiresAtEpochMs(),
                  snapshot.port(),
                  snapshot.preload(),
                  snapshot.durationSeconds(),
                  snapshot.rawValue());
      Ircv3StsPersistedPolicyNormalizer.Result normalized =
          persistedPolicyNormalizer.normalize(entry.getKey(), featureSnapshot, now);
      if (normalized.forgetPersisted()) {
        dropped++;
        forgetPersistedPolicy(normalized.hostLower());
        continue;
      }
      Ircv3StsPolicy policy = normalized.policy().orElse(null);
      if (policy == null) continue;
      byHostLower.put(policy.hostLower(), policy);
      loaded++;
    }

    if (loaded > 0 || dropped > 0) {
      log.info("[ircafe] restored {} persisted STS policies (dropped {} expired)", loaded, dropped);
    }
  }

  private void applyLearningDecision(
      String serverId, Ircv3StsPolicyLearningPlanner.Decision decision) {
    switch (decision.outcome()) {
      case IGNORE_MISSING_HOST, IGNORE_EMPTY_VALUE -> {
        return;
      }
      case IGNORE_INSECURE_CONNECTION -> {
        log.debug(
            "[{}] ignoring STS policy for host={} because connection is not secure (value={})",
            serverId,
            decision.hostLower(),
            decision.rawValue());
        return;
      }
      case IGNORE_INVALID_DIRECTIVE -> {
        log.warn(
            "[{}] ignoring invalid STS policy for host={}: {}",
            serverId,
            decision.hostLower(),
            decision.rawValue());
        return;
      }
      case CLEAR -> {
        byHostLower.remove(decision.hostLower());
        forgetPersistedPolicy(decision.hostLower());
        log.info("[{}] cleared STS policy for host={} (duration=0)", serverId, decision.hostLower());
        return;
      }
      case LEARN -> {
        Ircv3StsPolicy policy = decision.policy().orElseThrow();
        byHostLower.put(policy.hostLower(), policy);
        persistPolicy(policy);
        log.info(
            "[{}] learned STS policy host={} duration={}s port={} preload={} expiresAt={}",
            serverId,
            policy.hostLower(),
            policy.durationSeconds(),
            policy.port(),
            policy.preload(),
            policy.expiresAtEpochMs());
      }
    }
  }

  private void persistPolicy(Ircv3StsPolicy policy) {
    Ircv3StsPolicyConfigPort store = runtimeConfig;
    if (store == null || policy == null) return;
    store.rememberIrcv3StsPolicy(
        policy.hostLower(),
        policy.expiresAtEpochMs(),
        policy.port(),
        policy.preload(),
        policy.durationSeconds(),
        policy.rawValue());
  }

  private void forgetPersistedPolicy(String hostLower) {
    Ircv3StsPolicyConfigPort store = runtimeConfig;
    if (store == null || hostLower == null || hostLower.isBlank()) return;
    store.forgetIrcv3StsPolicy(hostLower);
  }
}
