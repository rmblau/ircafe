package cafe.woden.ircclient.notify.api.irc;

/** Normalized seed values for IRC-event notification rule edit surfaces. */
public record IrcEventNotificationRuleEditSeedPlan(
    boolean enabled,
    String eventType,
    String sourceMode,
    String sourcePattern,
    String channelScope,
    String channelPatterns,
    boolean toastEnabled,
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
    String ctcpValuePattern) {}
