package cafe.woden.ircclient.notify.api.irc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Feature-owned matching policy for IRC event notification rules. */
public final class IrcEventNotificationMatchPolicy {
  private static final String CTCP_RECEIVED = "CTCP_RECEIVED";

  private IrcEventNotificationMatchPolicy() {}

  public static boolean matches(
      IrcEventNotificationMatchRule rule, IrcEventNotificationMatchEvent event) {
    if (rule == null || event == null) return false;
    if (!rule.enabled()) return false;
    if (event.eventType() == null || !Objects.equals(rule.eventType(), event.eventType())) {
      return false;
    }
    if (!matchesSource(rule, event.sourceNick(), event.sourceIsSelf())) return false;
    if (!matchesCtcp(rule, event.ctcpCommand(), event.ctcpValue())) return false;
    return matchesChannel(
        rule, event.channel(), event.activeTargetOnSameServer(), event.activeTarget());
  }

  public static boolean hasEnabledRuleFor(
      Iterable<IrcEventNotificationMatchRule> rules, String eventType) {
    String type = normalizeToNull(eventType);
    if (rules == null || type == null) return false;

    for (IrcEventNotificationMatchRule rule : rules) {
      if (rule == null) continue;
      if (!rule.enabled()) continue;
      if (Objects.equals(rule.eventType(), type)) return true;
    }
    return false;
  }

  public static boolean matchesCtcp(
      IrcEventNotificationMatchRule rule, String command, String value) {
    if (rule == null) return false;
    if (!CTCP_RECEIVED.equals(rule.eventType())) return true;
    if (!matchesCtcpMode(rule.ctcpCommandMode(), rule.ctcpCommandPattern(), command)) {
      return false;
    }
    return matchesCtcpMode(rule.ctcpValueMode(), rule.ctcpValuePattern(), value);
  }

  public static boolean matchesSource(
      IrcEventNotificationMatchRule rule, String sourceNick, Boolean sourceIsSelf) {
    if (rule == null) return false;
    return switch (mode(rule.sourceMode(), "ANY")) {
      case "ANY" -> true;
      case "SELF" -> Boolean.TRUE.equals(sourceIsSelf);
      case "OTHERS" -> Boolean.FALSE.equals(sourceIsSelf);
      case "NICK_LIST" -> matchesNickList(rule.sourcePattern(), sourceNick);
      case "GLOB" -> {
        String nick = normalizeToNull(sourceNick);
        if (nick == null) {
          yield false;
        }
        yield matchesAnyMask(parseMaskList(rule.sourcePattern()), nick);
      }
      case "REGEX" -> regexMatch(rule.sourcePattern(), sourceNick);
      default -> true;
    };
  }

  public static boolean matchesChannel(IrcEventNotificationMatchRule rule, String channel) {
    return matchesChannel(rule, channel, false, null);
  }

  public static boolean matchesChannel(
      IrcEventNotificationMatchRule rule,
      String channel,
      boolean activeTargetOnSameServer,
      String activeTarget) {
    if (rule == null) return false;
    String ch = normalizeToNull(channel);
    String active = normalizeToNull(activeTarget);
    List<String> masks = parseMaskList(rule.channelPatterns());

    return switch (mode(rule.channelScope(), "ALL")) {
      case "ALL" -> true;
      case "ACTIVE_TARGET_ONLY" ->
          activeTargetOnSameServer && ch != null && active != null && ch.equalsIgnoreCase(active);
      case "ONLY" -> ch != null && !masks.isEmpty() && matchesAnyMask(masks, ch);
      case "ALL_EXCEPT" -> ch == null || masks.isEmpty() || !matchesAnyMask(masks, ch);
      default -> true;
    };
  }

  private static boolean matchesNickList(String rawList, String nick) {
    String normalizedNick = normalizeToNull(nick);
    if (normalizedNick == null) return false;

    for (String token : parseTokenList(rawList)) {
      if (token.equalsIgnoreCase(normalizedNick)) return true;
    }
    return false;
  }

  private static boolean matchesCtcpMode(String mode, String pattern, String value) {
    String effective = mode(mode, "ANY");
    if ("ANY".equals(effective)) return true;

    String p = normalizeToNull(pattern);
    String v = normalizeToNull(value);
    if (p == null || v == null) return false;

    return switch (effective) {
      case "LIKE" -> p.equalsIgnoreCase(v);
      case "GLOB" -> globMatch(p.toLowerCase(Locale.ROOT), v.toLowerCase(Locale.ROOT));
      case "REGEX" -> regexMatch(p, v);
      default -> true;
    };
  }

  private static boolean regexMatch(String regex, String value) {
    String r = normalizeToNull(regex);
    String v = normalizeToNull(value);
    if (r == null || v == null) return false;

    try {
      return Pattern.compile(r, Pattern.CASE_INSENSITIVE).matcher(v).matches();
    } catch (PatternSyntaxException ignored) {
      return false;
    }
  }

  private static String normalizeToNull(String raw) {
    String value = Objects.toString(raw, "").trim();
    return value.isEmpty() ? null : value;
  }

  private static List<String> parseTokenList(String raw) {
    String s = normalizeToNull(raw);
    if (s == null) return List.of();

    String[] tokens = s.split("[,\\s]+");
    List<String> out = new ArrayList<>();
    for (String token : tokens) {
      String t = normalizeToNull(token);
      if (t == null) continue;
      out.add(t);
    }
    return out;
  }

  private static List<String> parseMaskList(String raw) {
    List<String> tokens = parseTokenList(raw);
    if (tokens.isEmpty()) return List.of();

    List<String> out = new ArrayList<>(tokens.size());
    for (String token : tokens) {
      out.add(token.toLowerCase(Locale.ROOT));
    }
    return out;
  }

  private static boolean matchesAnyMask(List<String> masks, String value) {
    if (masks == null || masks.isEmpty()) return false;
    String v = normalizeToNull(value);
    if (v == null) return false;
    String normalized = v.toLowerCase(Locale.ROOT);

    for (String mask : masks) {
      if (globMatch(mask, normalized)) return true;
    }
    return false;
  }

  private static boolean globMatch(String mask, String value) {
    if (mask == null || mask.isEmpty()) return false;
    StringBuilder regex = new StringBuilder("^");
    for (int i = 0; i < mask.length(); i++) {
      char c = mask.charAt(i);
      if (c == '*') {
        regex.append(".*");
        continue;
      }
      if (c == '?') {
        regex.append('.');
        continue;
      }
      if ("\\.[]{}()+-^$|".indexOf(c) >= 0) {
        regex.append('\\');
      }
      regex.append(c);
    }
    regex.append('$');
    return Pattern.compile(regex.toString()).matcher(value).matches();
  }

  private static String mode(String raw, String fallback) {
    String normalized = Objects.toString(raw, "").trim().toUpperCase(Locale.ROOT);
    return normalized.isEmpty() ? fallback : normalized;
  }
}
