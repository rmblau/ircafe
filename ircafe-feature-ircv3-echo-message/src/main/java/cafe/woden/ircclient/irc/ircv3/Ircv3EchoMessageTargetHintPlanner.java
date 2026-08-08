package cafe.woden.ircclient.irc.ircv3;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Plans private-conversation target hints from self-authored echo-message traffic. */
public final class Ircv3EchoMessageTargetHintPlanner {

  private Ircv3EchoMessageTargetHintPlanner() {}

  public record TargetHint(
      String fromNick, String target, String kind, String payload, String messageId) {
    public TargetHint {
      fromNick = normalize(fromNick);
      target = normalize(target);
      kind = normalize(kind).toUpperCase(Locale.ROOT);
      payload = Objects.toString(payload, "");
      messageId = normalize(messageId);
      if (fromNick.isEmpty()) throw new IllegalArgumentException("fromNick must not be blank");
      if (target.isEmpty()) throw new IllegalArgumentException("target must not be blank");
      if (!"PRIVMSG".equals(kind) && !"ACTION".equals(kind)) {
        throw new IllegalArgumentException("kind must be PRIVMSG or ACTION");
      }
    }
  }

  public static Optional<TargetHint> plan(
      String sourceNick,
      String rawTarget,
      String command,
      String rawLine,
      List<String> parsedLine,
      Map<String, String> tags,
      Collection<String> selfNickAliases) {
    String from = normalize(sourceNick);
    if (from.isEmpty() || !matchesAny(from, selfNickAliases)) return Optional.empty();

    String cmd = normalize(command).toUpperCase(Locale.ROOT);
    if (!"PRIVMSG".equals(cmd)) return Optional.empty();

    String messageTarget = stripLeadingColon(rawTarget);
    if (messageTarget.isEmpty()) {
      messageTarget = firstParam(parsedLine);
    }
    if (messageTarget.isEmpty()
        || Ircv3ChannelContextPolicy.isChannelName(messageTarget)
        || matchesAny(messageTarget, selfNickAliases)) {
      return Optional.empty();
    }

    String first = firstParam(parsedLine);
    String second = secondParam(parsedLine);
    String payload = second;
    if (payload.isEmpty() && !first.isEmpty() && !first.equalsIgnoreCase(messageTarget)) {
      payload = first;
    }
    if (payload.isEmpty()) {
      payload = trailingParam(rawLine);
    }

    String action = parseCtcpAction(payload);
    String kind = action == null ? "PRIVMSG" : "ACTION";
    String normalizedPayload = action == null ? payload : action;
    String messageId =
        Ircv3Tags.firstDecodedTagValue(
            tags, "msgid", "+msgid", "draft/msgid", "+draft/msgid");

    return Optional.of(
        new TargetHint(from, messageTarget, kind, normalizedPayload, messageId));
  }

  private static boolean matchesAny(String value, Collection<String> aliases) {
    String normalized = normalize(value);
    if (normalized.isEmpty() || aliases == null || aliases.isEmpty()) return false;
    for (String alias : aliases) {
      String candidate = normalize(alias);
      if (!candidate.isEmpty() && normalized.equalsIgnoreCase(candidate)) return true;
    }
    return false;
  }

  private static String firstParam(List<String> parsedLine) {
    if (parsedLine == null || parsedLine.isEmpty()) return "";
    return stripLeadingColon(parsedLine.get(0));
  }

  private static String secondParam(List<String> parsedLine) {
    if (parsedLine == null || parsedLine.size() < 2) return "";
    return stripLeadingColon(parsedLine.get(1));
  }

  private static String trailingParam(String rawLine) {
    String line = Objects.toString(rawLine, "");
    int idx = line.indexOf(" :");
    if (idx < 0 || idx + 2 >= line.length()) return "";
    return trimProtocolWhitespace(line.substring(idx + 2));
  }

  private static String parseCtcpAction(String message) {
    if (message == null || message.length() < 2) return null;
    if (message.charAt(0) != 0x01 || message.charAt(message.length() - 1) != 0x01) return null;
    String inner = message.substring(1, message.length() - 1).trim();
    if (!inner.regionMatches(true, 0, "ACTION", 0, 6)) return null;
    return inner.length() > 6 ? inner.substring(6).trim() : "";
  }

  private static String stripLeadingColon(String raw) {
    String value = trimProtocolWhitespace(Objects.toString(raw, ""));
    if (value.startsWith(":")) {
      value = trimProtocolWhitespace(value.substring(1));
    }
    return value;
  }

  private static String trimProtocolWhitespace(String value) {
    int start = 0;
    int end = value.length();
    while (start < end && isProtocolWhitespace(value.charAt(start))) start++;
    while (end > start && isProtocolWhitespace(value.charAt(end - 1))) end--;
    return value.substring(start, end);
  }

  private static boolean isProtocolWhitespace(char value) {
    return value == ' ' || value == '\t' || value == '\r' || value == '\n';
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }
}
