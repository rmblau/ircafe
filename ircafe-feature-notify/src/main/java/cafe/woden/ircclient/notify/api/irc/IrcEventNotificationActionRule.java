package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Feature-safe action settings for a matched IRC event notification rule. */
public record IrcEventNotificationActionRule(
    boolean notificationsNodeEnabled,
    boolean toastEnabled,
    String focusScope,
    boolean statusBarEnabled,
    boolean soundEnabled,
    String soundId,
    boolean soundUseCustom,
    String soundCustomPath,
    boolean scriptEnabled,
    String scriptPath,
    String scriptArgs,
    String scriptWorkingDirectory) {

  public IrcEventNotificationActionRule {
    focusScope = normalizeToNull(focusScope);
    soundId = normalizeToNull(soundId);
    soundCustomPath = normalizeToNull(soundCustomPath);
    if (soundUseCustom && soundCustomPath == null) {
      soundUseCustom = false;
    }
    scriptPath = normalizeToNull(scriptPath);
    if (scriptEnabled && scriptPath == null) {
      scriptEnabled = false;
    }
    scriptArgs = normalizeToNull(scriptArgs);
    scriptWorkingDirectory = normalizeToNull(scriptWorkingDirectory);
  }

  private static String normalizeToNull(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
