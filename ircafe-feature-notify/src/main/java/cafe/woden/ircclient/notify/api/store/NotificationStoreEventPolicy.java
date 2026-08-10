package cafe.woden.ircclient.notify.api.store;

import java.util.Objects;

/** Feature-owned normalization rules for notification-store event values. */
public final class NotificationStoreEventPolicy {
  public static final String DEFAULT_NICK = "?";
  public static final String DEFAULT_RULE_LABEL = "(rule)";
  public static final String DEFAULT_IRC_EVENT_TARGET = "status";
  public static final int MAX_SNIPPET_CHARS = 400;

  private NotificationStoreEventPolicy() {}

  public static NotificationStoreEventValues highlight(
      String serverId, String channel, String fromNick, String snippet, String messageId) {
    String sid = normalizeServerId(serverId);
    String chan = normalizeChannel(channel);
    if (sid.isEmpty() || chan.isEmpty()) {
      return NotificationStoreEventValues.invalid();
    }
    return new NotificationStoreEventValues(
        true,
        sid,
        chan,
        normalizeNick(fromNick),
        "",
        normalizeSnippet(snippet),
        normalizeMessageId(messageId));
  }

  public static NotificationStoreEventValues ruleMatch(
      String serverId,
      String channel,
      String fromNick,
      String ruleLabel,
      String snippet,
      String messageId) {
    String sid = normalizeServerId(serverId);
    String chan = normalizeChannel(channel);
    if (sid.isEmpty() || chan.isEmpty()) {
      return NotificationStoreEventValues.invalid();
    }
    return new NotificationStoreEventValues(
        true,
        sid,
        chan,
        normalizeNick(fromNick),
        normalizeLabel(ruleLabel),
        normalizeSnippet(snippet),
        normalizeMessageId(messageId));
  }

  public static NotificationStoreEventValues ircEvent(
      String serverId,
      String target,
      String fromNick,
      String title,
      String body,
      String messageId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) {
      return NotificationStoreEventValues.invalid();
    }

    String chan = normalizeChannel(target);
    if (chan.isEmpty()) {
      chan = DEFAULT_IRC_EVENT_TARGET;
    }

    return new NotificationStoreEventValues(
        true,
        sid,
        chan,
        normalizeNick(fromNick),
        normalizeLabel(title),
        normalizeSnippet(body),
        normalizeMessageId(messageId));
  }

  public static String normalizeServerId(String serverId) {
    return Objects.toString(serverId, "").trim();
  }

  public static String normalizeChannel(String channel) {
    return Objects.toString(channel, "").trim();
  }

  public static String normalizeNick(String nick) {
    String normalized = Objects.toString(nick, "").trim();
    return normalized.isEmpty() ? DEFAULT_NICK : normalized;
  }

  public static String normalizeMessageId(String messageId) {
    return Objects.toString(messageId, "").trim();
  }

  public static String normalizeLabel(String label) {
    String normalized = Objects.toString(label, "").trim();
    return normalized.isEmpty() ? DEFAULT_RULE_LABEL : normalized;
  }

  public static String normalizeSnippet(String snippet) {
    String normalized = Objects.toString(snippet, "").trim();
    if (normalized.isEmpty()) return "";
    if (normalized.length() > MAX_SNIPPET_CHARS) {
      return normalized.substring(0, MAX_SNIPPET_CHARS - 1) + "\u2026";
    }
    return normalized;
  }
}
