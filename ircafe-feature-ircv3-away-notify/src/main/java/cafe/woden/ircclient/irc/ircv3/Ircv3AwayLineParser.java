package cafe.woden.ircclient.irc.ircv3;

import java.util.Arrays;
import java.util.Objects;

/** Parses raw away-notify commands and self-away confirmation numerics. */
public final class Ircv3AwayLineParser {

  private Ircv3AwayLineParser() {}

  public record AwayNotify(String nick, boolean away, String message) {
    public AwayNotify {
      nick = Objects.toString(nick, "").trim();
      message = normalizeNullable(message);
    }
  }

  public record AwayConfirmation(boolean away, String server, String message) {
    public AwayConfirmation {
      server = Objects.toString(server, "").trim();
      message = normalizeNullable(message);
    }
  }

  public static AwayNotify parseAwayNotify(String line) {
    if (line == null) return null;
    String normalized = line.trim();
    if (!normalized.startsWith(":")) return null;

    int firstSpace = normalized.indexOf(' ');
    if (firstSpace <= 1 || firstSpace + 1 >= normalized.length()) return null;
    String prefix = normalized.substring(1, firstSpace);
    String rest = normalized.substring(firstSpace + 1).trim();
    if (!(rest.startsWith("AWAY")
        && (rest.length() == 4 || Character.isWhitespace(rest.charAt(4))))) {
      return null;
    }

    String nick = prefix;
    int bang = nick.indexOf('!');
    if (bang > 0) nick = nick.substring(0, bang);
    nick = nick.trim();
    if (nick.isBlank()) return null;

    boolean away = rest.length() > 4;
    String message = null;
    if (away) {
      String remainder = rest.substring(4).trim();
      if (remainder.startsWith(":")) remainder = remainder.substring(1).trim();
      message = remainder;
    }
    return new AwayNotify(nick, away, message);
  }

  public static AwayConfirmation parseRpl305or306Away(String line) {
    if (line == null) return null;
    String normalized = line.trim();
    if (normalized.isEmpty()) return null;

    String server = "";
    if (normalized.startsWith(":")) {
      int firstSpace = normalized.indexOf(' ');
      if (firstSpace > 1) {
        server = normalized.substring(1, firstSpace);
        if (firstSpace + 1 < normalized.length()) {
          normalized = normalized.substring(firstSpace + 1).trim();
        }
      }
    }

    boolean is305 =
        normalized.startsWith("305 ") || normalized.startsWith("305\t") || normalized.equals("305");
    boolean is306 =
        normalized.startsWith("306 ") || normalized.startsWith("306\t") || normalized.equals("306");
    if (!is305 && !is306) return null;

    String message = null;
    int trailing = normalized.indexOf(" :");
    if (trailing >= 0 && trailing + 2 < normalized.length()) {
      message = normalized.substring(trailing + 2).trim();
    } else {
      String[] tokens = normalized.split("\\s+");
      if (tokens.length >= 3) {
        message = String.join(" ", Arrays.copyOfRange(tokens, 2, tokens.length)).trim();
      }
    }
    return new AwayConfirmation(is306, server, message);
  }

  private static String normalizeNullable(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
