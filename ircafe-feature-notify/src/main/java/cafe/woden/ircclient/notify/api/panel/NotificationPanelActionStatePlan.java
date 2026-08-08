package cafe.woden.ircclient.notify.api.panel;

/** Feature-owned enablement plan for notification panel row actions. */
public record NotificationPanelActionStatePlan(
    boolean jumpToMessageEnabled,
    boolean clearSelectedEnabled,
    boolean clearAllEnabled,
    boolean exportSelectedEnabled,
    boolean exportAllEnabled) {}
