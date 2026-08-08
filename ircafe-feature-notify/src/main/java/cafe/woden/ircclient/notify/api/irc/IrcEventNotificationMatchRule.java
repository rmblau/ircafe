package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Feature-safe IRC event notification rule values used by the matcher. */
public record IrcEventNotificationMatchRule(
    boolean enabled,
    String eventType,
    String sourceMode,
    String sourcePattern,
    String channelScope,
    String channelPatterns,
    String ctcpCommandMode,
    String ctcpCommandPattern,
    String ctcpValueMode,
    String ctcpValuePattern) {

  public IrcEventNotificationMatchRule {
    eventType = trimToNull(eventType);
    sourceMode = defaultMode(sourceMode, "ANY");
    channelScope = defaultMode(channelScope, "ALL");
    ctcpCommandMode = defaultMode(ctcpCommandMode, "ANY");
    ctcpValueMode = defaultMode(ctcpValueMode, "ANY");
  }

  private static String defaultMode(String raw, String fallback) {
    String value = Objects.toString(raw, "").trim();
    return value.isEmpty() ? fallback : value;
  }

  private static String trimToNull(String raw) {
    String value = Objects.toString(raw, "").trim();
    return value.isEmpty() ? null : value;
  }
}
