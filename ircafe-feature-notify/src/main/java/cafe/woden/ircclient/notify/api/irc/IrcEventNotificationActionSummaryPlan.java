package cafe.woden.ircclient.notify.api.irc;

/** Feature-safe, UI-label-free summary of enabled IRC-event notification actions. */
public record IrcEventNotificationActionSummaryPlan(
    boolean toastEnabled,
    String focusScope,
    boolean statusBarEnabled,
    boolean notificationsNodeEnabled,
    boolean soundEnabled,
    boolean customSound,
    String soundId,
    boolean scriptEnabled,
    String scriptPath) {

  public IrcEventNotificationActionSummaryPlan {
    focusScope = normalizeToNull(focusScope);
    soundId = normalizeToNull(soundId);
    scriptPath = normalizeToNull(scriptPath);
    if (!soundEnabled) {
      customSound = false;
      soundId = null;
    }
    if (!scriptEnabled) {
      scriptPath = null;
    }
  }

  public static IrcEventNotificationActionSummaryPlan none() {
    return new IrcEventNotificationActionSummaryPlan(
        false, null, false, false, false, false, null, false, null);
  }

  public String scriptLeafName() {
    if (scriptPath == null) return null;
    int slash = Math.max(scriptPath.lastIndexOf('/'), scriptPath.lastIndexOf('\\'));
    return slash >= 0 && slash < scriptPath.length() - 1
        ? scriptPath.substring(slash + 1)
        : scriptPath;
  }

  private static String normalizeToNull(String value) {
    if (value == null) return null;
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
