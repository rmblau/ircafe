package cafe.woden.ircclient.bouncer;

import java.util.Collection;
import java.util.Objects;

/** Feature-owned origin-disconnect planning for generic bouncer ephemeral lifecycle cleanup. */
public final class BouncerOriginDisconnectPlanner {

  public BouncerOriginDisconnectPlan plan(
      String originServerId, Collection<String> activeEphemeralOrigins) {
    String origin = normalize(originServerId);
    if (origin == null) {
      return BouncerOriginDisconnectPlan.skipInvalidOrigin();
    }

    long count = countMatchingOrigins(origin, activeEphemeralOrigins);
    if (count == 0) {
      return BouncerOriginDisconnectPlan.skipNoMatchingEphemerals(origin);
    }
    return BouncerOriginDisconnectPlan.clearOrigin(origin, count);
  }

  private static long countMatchingOrigins(
      String origin, Collection<String> activeEphemeralOrigins) {
    if (activeEphemeralOrigins == null || activeEphemeralOrigins.isEmpty()) {
      return 0;
    }
    return activeEphemeralOrigins.stream().filter(o -> origin.equals(normalize(o))).count();
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v;
  }
}
