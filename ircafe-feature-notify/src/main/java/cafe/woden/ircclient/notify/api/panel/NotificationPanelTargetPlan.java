package cafe.woden.ircclient.notify.api.panel;

/** Feature-owned normalized target details for notification panel navigation actions. */
public record NotificationPanelTargetPlan(boolean valid, String serverId, String channel) {}
