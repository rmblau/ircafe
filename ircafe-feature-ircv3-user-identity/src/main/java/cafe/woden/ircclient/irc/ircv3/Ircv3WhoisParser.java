package cafe.woden.ircclient.irc.ircv3;

/** Transport-neutral parsing policy for WHOIS and WHOWAS numerics. */
public final class Ircv3WhoisParser {
  private Ircv3WhoisParser() {}

  public record ParsedWhoisUser(String nick, String user, String host) {}

  public record ParsedWhoisAway(String nick, String message) {}

  public record ParsedWhoisAccount(String nick, String account) {}

  public static ParsedWhoisUser parseRpl311WhoisUser(String line) {
    return parseUserNumeric(line, "311");
  }

  public static ParsedWhoisUser parseRpl314WhowasUser(String line) {
    return parseUserNumeric(line, "314");
  }

  private static ParsedWhoisUser parseUserNumeric(String line, String numeric) {
    String s = normalizeLine(line);
    if (s == null) return null;

    String[] tokens = s.split("\\s+");
    if (tokens.length < 5 || !numeric.equals(tokens[0])) return null;

    String nick = tokens[2];
    String user = tokens[3];
    String host = tokens[4];
    if (nick.isBlank() || user.isBlank() || host.isBlank()) return null;
    return new ParsedWhoisUser(nick, user, host);
  }

  public static ParsedWhoisAway parseRpl301WhoisAway(String line) {
    String s = normalizeLine(line);
    if (s == null) return null;

    String[] tokens = s.split("\\s+");
    if (tokens.length < 3 || !"301".equals(tokens[0])) return null;

    String nick = tokens[2];
    if (nick.isBlank()) return null;

    String message = null;
    int trailing = s.indexOf(" :");
    if (trailing >= 0 && trailing + 2 < s.length()) {
      message = s.substring(trailing + 2).trim();
    }
    return new ParsedWhoisAway(nick, message);
  }

  public static String parseRpl318EndOfWhoisNick(String line) {
    String s = normalizeLine(line);
    if (s == null) return null;

    String[] tokens = s.split("\\s+");
    if (tokens.length < 3 || !"318".equals(tokens[0])) return null;

    String nick = tokens[2];
    return nick.isBlank() ? null : nick;
  }

  public static ParsedWhoisAccount parseRpl330WhoisAccount(String line) {
    String s = normalizeLine(line);
    if (s == null) return null;

    String[] tokens = s.split("\\s+");
    if (tokens.length < 4 || !"330".equals(tokens[0])) return null;

    String nick = tokens[2];
    String account = tokens[3].trim();
    if (nick.isBlank() || account.isEmpty() || "*".equals(account) || "0".equals(account)) {
      return null;
    }
    return new ParsedWhoisAccount(nick, account);
  }

  private static String normalizeLine(String line) {
    if (line == null) return null;
    String value = line.trim();
    if (value.isEmpty()) return null;

    if (value.startsWith("@")) {
      int space = value.indexOf(' ');
      if (space < 0 || space + 1 >= value.length()) return null;
      value = value.substring(space + 1).trim();
    }
    if (value.startsWith(":")) {
      int space = value.indexOf(' ');
      if (space < 0 || space + 1 >= value.length()) return null;
      value = value.substring(space + 1).trim();
    }
    return value.isEmpty() ? null : value;
  }
}
