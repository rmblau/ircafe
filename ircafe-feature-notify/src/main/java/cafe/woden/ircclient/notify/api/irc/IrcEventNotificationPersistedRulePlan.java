package cafe.woden.ircclient.notify.api.irc;

/** Normalized values for persisted IRC-event notification rule settings. */
public record IrcEventNotificationPersistedRulePlan(
    boolean enabled,
    String eventType,
    String sourceMode,
    String sourcePattern,
    String channelScope,
    String channelPatterns,
    boolean toastEnabled,
    boolean toastWhenFocused,
    String focusScope,
    boolean statusBarEnabled,
    boolean notificationsNodeEnabled,
    boolean soundEnabled,
    String soundId,
    boolean soundUseCustom,
    String soundCustomPath,
    boolean scriptEnabled,
    String scriptPath,
    String scriptArgs,
    String scriptWorkingDirectory,
    String ctcpCommandMode,
    String ctcpCommandPattern,
    String ctcpValueMode,
    String ctcpValuePattern,
    String channelWhitelist,
    String channelBlacklist) {}
