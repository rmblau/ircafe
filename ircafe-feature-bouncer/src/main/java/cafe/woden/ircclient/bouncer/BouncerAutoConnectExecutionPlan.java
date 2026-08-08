package cafe.woden.ircclient.bouncer;

import java.util.Objects;

/** Feature-owned decision for whether a discovered bouncer network should auto-connect now. */
public record BouncerAutoConnectExecutionPlan(
    Action action, String bouncerId, String networkName, String serverId) {

  public enum Action {
    SKIP_INVALID_SERVER_ID,
    SKIP_DISABLED,
    SKIP_ALREADY_QUEUED,
    CONNECT
  }

  public BouncerAutoConnectExecutionPlan {
    action = Objects.requireNonNull(action, "action");
    bouncerId = normalize(bouncerId);
    networkName = normalize(networkName);
    serverId = normalize(serverId);
  }

  public static BouncerAutoConnectExecutionPlan skipInvalidServerId(
      String bouncerId, String networkName) {
    return new BouncerAutoConnectExecutionPlan(
        Action.SKIP_INVALID_SERVER_ID, bouncerId, networkName, null);
  }

  public static BouncerAutoConnectExecutionPlan skipDisabled(
      String bouncerId, String networkName, String serverId) {
    return new BouncerAutoConnectExecutionPlan(
        Action.SKIP_DISABLED, bouncerId, networkName, serverId);
  }

  public static BouncerAutoConnectExecutionPlan skipAlreadyQueued(
      String bouncerId, String networkName, String serverId) {
    return new BouncerAutoConnectExecutionPlan(
        Action.SKIP_ALREADY_QUEUED, bouncerId, networkName, serverId);
  }

  public static BouncerAutoConnectExecutionPlan connect(
      String bouncerId, String networkName, String serverId) {
    return new BouncerAutoConnectExecutionPlan(Action.CONNECT, bouncerId, networkName, serverId);
  }

  public boolean connects() {
    return action == Action.CONNECT;
  }

  public boolean skips() {
    return !connects();
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v;
  }
}
