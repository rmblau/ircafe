package cafe.woden.ircclient.bouncer;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Feature-owned guard that prevents duplicate auto-connect attempts for discovered networks. */
public final class BouncerAutoConnectQueueGate {

  private final Set<String> queuedServerIds = ConcurrentHashMap.newKeySet();
  private final BouncerDiscoveryRuntimeRules runtimeRules;

  public BouncerAutoConnectQueueGate() {
    this(new BouncerDiscoveryRuntimeRules());
  }

  BouncerAutoConnectQueueGate(BouncerDiscoveryRuntimeRules runtimeRules) {
    this.runtimeRules = Objects.requireNonNull(runtimeRules, "runtimeRules");
  }

  public boolean markQueued(String serverId) {
    String sid = normalize(serverId);
    return sid != null && queuedServerIds.add(sid);
  }

  public void clearOrigin(String originServerId) {
    String origin = normalize(originServerId);
    if (origin == null) return;
    queuedServerIds.removeIf(serverId -> runtimeRules.originMatchesServerId(serverId, origin));
  }

  public boolean isQueued(String serverId) {
    String sid = normalize(serverId);
    return sid != null && queuedServerIds.contains(sid);
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v;
  }
}
