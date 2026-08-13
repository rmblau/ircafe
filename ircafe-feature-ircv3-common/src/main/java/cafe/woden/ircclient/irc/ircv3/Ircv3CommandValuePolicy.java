package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;

/** Shared normalization and IRCv3 tag escaping for outbound capability commands. */
public final class Ircv3CommandValuePolicy {

  private Ircv3CommandValuePolicy() {}

  public static String normalizeTarget(String target) {
    return Objects.toString(target, "").trim();
  }

  public static String normalizeToken(String value) {
    String token = Objects.toString(value, "").trim();
    if (token.isEmpty()) return "";
    for (int i = 0; i < token.length(); i++) {
      if (Character.isWhitespace(token.charAt(i))) return "";
    }
    return token;
  }

  public static String normalizeTagValue(String value) {
    return Objects.toString(value, "").trim();
  }

  public static String normalizeText(String value) {
    return Objects.toString(value, "").trim();
  }

  public static String escapeTagValue(String value) {
    String raw = Objects.toString(value, "");
    if (raw.isEmpty()) return "";
    StringBuilder out = new StringBuilder(raw.length() + 8);
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      switch (c) {
        case ';' -> out.append("\\:");
        case ' ' -> out.append("\\s");
        case '\\' -> out.append("\\\\");
        case '\r' -> out.append("\\r");
        case '\n' -> out.append("\\n");
        default -> out.append(c);
      }
    }
    return out.toString();
  }
}
