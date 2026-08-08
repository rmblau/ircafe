package cafe.woden.ircclient.notify.api.irc;

/**
 * Feature-owned, UI-label-free display bounds for IRC-event notification table summaries.
 *
 * <p>The root Swing table still owns localized labels and sentence assembly; this plan only carries
 * already-normalized/bounded values that are safe to insert into those localized strings.
 */
public record IrcEventNotificationTableSummaryDisplayPlan(
    String sourcePattern,
    String channelPatterns,
    String ctcpCommandPattern,
    String ctcpValuePattern,
    String scriptLeafName) {}
