package cafe.woden.ircclient.bouncer;

import java.util.Objects;

/** Feature-owned rules for deciding how a resolved bouncer discovery should be applied. */
public final class BouncerDiscoveredNetworkApplicationPlanner {

  public BouncerDiscoveredNetworkApplicationPlan plan(
      String serverId,
      String autoConnectName,
      boolean persistedServerExists,
      boolean existingEphemeralMatches,
      boolean existingOriginMatches) {
    String sid = requireNonBlank(serverId, "serverId");
    if (persistedServerExists) {
      return BouncerDiscoveredNetworkApplicationPlan.removeEphemeralDuplicate(sid);
    }
    if (existingEphemeralMatches && existingOriginMatches) {
      return BouncerDiscoveredNetworkApplicationPlan.keepExisting(sid);
    }
    return BouncerDiscoveredNetworkApplicationPlan.upsertEphemeral(sid, autoConnectName);
  }

  private static String requireNonBlank(String value, String field) {
    String v = Objects.toString(value, "").trim();
    if (v.isEmpty()) {
      throw new IllegalArgumentException(field + " is required");
    }
    return v;
  }
}
