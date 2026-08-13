package cafe.woden.ircclient.bouncer;

import java.util.Objects;

/** Feature-owned decision for applying a resolved discovered bouncer network. */
public record BouncerDiscoveredNetworkApplicationPlan(
    Action action, String serverId, String autoConnectName) {

  public enum Action {
    REMOVE_EPHEMERAL_DUPLICATE,
    KEEP_EXISTING,
    UPSERT_EPHEMERAL
  }

  public BouncerDiscoveredNetworkApplicationPlan {
    action = Objects.requireNonNull(action, "action");
    serverId = requireNonBlank(serverId, "serverId");
    autoConnectName = normalize(autoConnectName);
  }

  public static BouncerDiscoveredNetworkApplicationPlan removeEphemeralDuplicate(String serverId) {
    return new BouncerDiscoveredNetworkApplicationPlan(
        Action.REMOVE_EPHEMERAL_DUPLICATE, serverId, null);
  }

  public static BouncerDiscoveredNetworkApplicationPlan keepExisting(String serverId) {
    return new BouncerDiscoveredNetworkApplicationPlan(Action.KEEP_EXISTING, serverId, null);
  }

  public static BouncerDiscoveredNetworkApplicationPlan upsertEphemeral(
      String serverId, String autoConnectName) {
    return new BouncerDiscoveredNetworkApplicationPlan(
        Action.UPSERT_EPHEMERAL, serverId, autoConnectName);
  }

  public boolean removesEphemeralDuplicate() {
    return action == Action.REMOVE_EPHEMERAL_DUPLICATE;
  }

  public boolean keepsExisting() {
    return action == Action.KEEP_EXISTING;
  }

  public boolean upsertsEphemeral() {
    return action == Action.UPSERT_EPHEMERAL;
  }

  private static String requireNonBlank(String value, String field) {
    String v = normalize(value);
    if (v == null) {
      throw new IllegalArgumentException(field + " is required");
    }
    return v;
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v;
  }
}
