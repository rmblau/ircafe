package cafe.woden.ircclient.bouncer;

import java.util.Objects;

/** Feature-owned decision for clearing ephemeral networks after a bouncer origin disconnects. */
public record BouncerOriginDisconnectPlan(
    Action action, String originServerId, long ephemeralCount) {

  public enum Action {
    SKIP_INVALID_ORIGIN,
    SKIP_NO_MATCHING_EPHEMERALS,
    CLEAR_ORIGIN
  }

  public BouncerOriginDisconnectPlan {
    action = Objects.requireNonNull(action, "action");
    originServerId = normalize(originServerId);
    if (ephemeralCount < 0) {
      throw new IllegalArgumentException("ephemeralCount must not be negative");
    }
  }

  public static BouncerOriginDisconnectPlan skipInvalidOrigin() {
    return new BouncerOriginDisconnectPlan(Action.SKIP_INVALID_ORIGIN, null, 0);
  }

  public static BouncerOriginDisconnectPlan skipNoMatchingEphemerals(String originServerId) {
    return new BouncerOriginDisconnectPlan(Action.SKIP_NO_MATCHING_EPHEMERALS, originServerId, 0);
  }

  public static BouncerOriginDisconnectPlan clearOrigin(String originServerId, long ephemeralCount) {
    String origin = requireNonBlank(originServerId, "originServerId");
    if (ephemeralCount <= 0) {
      throw new IllegalArgumentException("ephemeralCount must be positive");
    }
    return new BouncerOriginDisconnectPlan(Action.CLEAR_ORIGIN, origin, ephemeralCount);
  }

  public boolean clearsOrigin() {
    return action == Action.CLEAR_ORIGIN;
  }

  public boolean skips() {
    return !clearsOrigin();
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
