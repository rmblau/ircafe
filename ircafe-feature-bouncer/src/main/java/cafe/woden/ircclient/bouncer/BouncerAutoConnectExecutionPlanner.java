package cafe.woden.ircclient.bouncer;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Feature-owned auto-connect execution rules for discovered bouncer networks. */
public final class BouncerAutoConnectExecutionPlanner {

  private final BouncerAutoConnectQueueGate queueGate;

  public BouncerAutoConnectExecutionPlanner(BouncerAutoConnectQueueGate queueGate) {
    this.queueGate = Objects.requireNonNull(queueGate, "queueGate");
  }

  public BouncerAutoConnectExecutionPlan plan(
      String bouncerId, String networkName, String serverId, BooleanSupplier autoConnectEnabled) {
    Objects.requireNonNull(autoConnectEnabled, "autoConnectEnabled");
    String sid = normalize(serverId);
    if (sid == null) {
      return BouncerAutoConnectExecutionPlan.skipInvalidServerId(bouncerId, networkName);
    }
    if (!autoConnectEnabled.getAsBoolean()) {
      return BouncerAutoConnectExecutionPlan.skipDisabled(bouncerId, networkName, sid);
    }
    if (!queueGate.markQueued(sid)) {
      return BouncerAutoConnectExecutionPlan.skipAlreadyQueued(bouncerId, networkName, sid);
    }
    return BouncerAutoConnectExecutionPlan.connect(bouncerId, networkName, sid);
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v;
  }
}
