package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Feature-safe script side-effect settings for a matched IRC event notification rule. */
public record IrcEventNotificationScriptAction(
    boolean enabled, String scriptPath, String scriptArgs, String workingDirectory) {

  public IrcEventNotificationScriptAction {
    scriptPath = normalizeToNull(scriptPath);
    scriptArgs = normalizeToNull(scriptArgs);
    workingDirectory = normalizeToNull(workingDirectory);
    if (enabled && scriptPath == null) {
      enabled = false;
    }
  }

  public static IrcEventNotificationScriptAction disabled() {
    return new IrcEventNotificationScriptAction(false, null, null, null);
  }

  public static IrcEventNotificationScriptAction of(
      boolean enabled, String scriptPath, String scriptArgs, String workingDirectory) {
    return new IrcEventNotificationScriptAction(enabled, scriptPath, scriptArgs, workingDirectory);
  }

  private static String normalizeToNull(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
