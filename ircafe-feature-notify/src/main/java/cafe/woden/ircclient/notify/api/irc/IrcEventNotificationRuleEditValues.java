package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Feature-safe values from one IRC-event notification rule edit form. */
public record IrcEventNotificationRuleEditValues(
    String eventType,
    String sourceMode,
    String sourcePattern,
    String channelScope,
    String channelPatterns,
    String ctcpCommandMode,
    String ctcpCommandPattern,
    String ctcpValueMode,
    String ctcpValuePattern,
    boolean scriptEnabled,
    String scriptPath) {

  public IrcEventNotificationRuleEditValues {
    eventType = normalize(eventType);
    sourceMode = normalize(sourceMode);
    sourcePattern = normalize(sourcePattern);
    channelScope = normalize(channelScope);
    channelPatterns = normalize(channelPatterns);
    ctcpCommandMode = normalize(ctcpCommandMode);
    ctcpCommandPattern = normalize(ctcpCommandPattern);
    ctcpValueMode = normalize(ctcpValueMode);
    ctcpValuePattern = normalize(ctcpValuePattern);
    scriptPath = normalize(scriptPath);
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }
}
