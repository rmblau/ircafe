package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Feature-safe default settings for an IRC event notification rule. */
public record IrcEventNotificationDefaultRule(
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
    String ctcpValuePattern) {

  public IrcEventNotificationDefaultRule {
    eventType = normalizeToNull(eventType);
    sourceMode = normalizeToNull(sourceMode);
    sourcePattern = normalizeToNull(sourcePattern);
    channelScope = normalizeToNull(channelScope);
    channelPatterns = normalizeToNull(channelPatterns);
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
    ctcpCommandMode = normalizeToNull(ctcpCommandMode);
    ctcpCommandPattern = normalizeToNull(ctcpCommandPattern);
    ctcpValueMode = normalizeToNull(ctcpValueMode);
    ctcpValuePattern = normalizeToNull(ctcpValuePattern);
  }

  private static String normalizeToNull(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
