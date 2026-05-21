package cafe.woden.ircclient.notifications.api;

/** Notification store update signal used by the UI to refresh. */
public record NotificationChange(String serverId) {}
