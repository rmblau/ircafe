package cafe.woden.ircclient.irc.ircv3;

import java.util.ArrayList;
import java.util.List;

/** Transport-neutral WHO, WHOX, and USERHOST parsing policy. */
public final class Ircv3WhoUserhostParser {
  private Ircv3WhoUserhostParser() {}
  /**
   * Parse RPL_ISUPPORT (005) and return true if WHOX token is present.
   *
   * <p>Servers may split ISUPPORT across multiple 005 lines. Call this on each line and treat a
   * single true result as sufficient evidence of WHOX support.
   */
  public static boolean parseRpl005IsupportHasWhox(String line) {
    return Boolean.TRUE.equals(parseRpl005IsupportWhoxSupport(line));
  }

  /**
   * Parse one RPL_ISUPPORT line for a WHOX token update.
   *
   * @return {@code true} for WHOX, {@code false} for -WHOX, or {@code null} when absent.
   */
  public static Boolean parseRpl005IsupportWhoxSupport(String line) {
    return Ircv3IsupportLine.parse(line)
        .flatMap(parsed -> parsed.lastToken("WHOX"))
        .map(token -> !token.removed())
        .orElse(null);
  }

  public enum AwayState {
    UNKNOWN,
    AWAY,
    HERE
  }

  public record ParsedWhoReply(
      String channel, String nick, String user, String host, String flags) {}

  public record ParsedWhoxReply(String channel, String nick, String user, String host) {}

  /** Strict parse for IRCafe-issued WHOX scans: WHO <chan> %tcuhnaf,<token> */
  public record ParsedWhoxTcuhnaf(
      String token,
      String channel,
      String user,
      String host,
      String nick,
      String flags,
      String account) {}

  public record UserhostEntry(String nick, String hostmask, AwayState awayState) {}

  public static ParsedWhoReply parseRpl352WhoReply(String line) {
    if (line == null) return null;
    String s = line.trim();
    if (s.isEmpty()) return null;
    if (s.startsWith(":")) {
      int sp = s.indexOf(' ');
      if (sp > 0 && sp + 1 < s.length()) s = s.substring(sp + 1).trim();
    }

    String[] toks = s.split("\\s+");
    if (toks.length < 8) return null;
    if (!"352".equals(toks[0])) return null;
    String channel = toks[2];
    String user = toks[3];
    String host = toks[4];
    String nick = toks[6];
    String flags = toks[7];

    if (channel == null || channel.isBlank()) return null;
    if (nick == null || nick.isBlank()) return null;
    if (user == null || user.isBlank()) return null;
    if (host == null || host.isBlank()) return null;

    if (flags == null) flags = "";

    return new ParsedWhoReply(channel, nick, user, host, flags);
  }

  /**
   * Strict parse for WHOX results for the IRCafe-issued field set: %tcuhnaf
   *
   * <p>Expected numeric form after stripping IRCv3 tags:
   *
   * <pre>
   * :server 354 <me> <token> <channel> <user> <host> <nick> <flags> <account> :<optional trailing>
   * </pre>
   *
   * We validate the token and channel-ish shape to avoid mis-parsing arbitrary WHOX formats.
   */
  public static ParsedWhoxTcuhnaf parseRpl354WhoxTcuhnaf(String line, String expectedToken) {
    if (line == null) return null;
    String s = line.trim();
    if (s.isEmpty()) return null;
    if (s.startsWith(":")) {
      int sp = s.indexOf(' ');
      if (sp > 0 && sp + 1 < s.length()) s = s.substring(sp + 1).trim();
    }
    int colon = s.indexOf(" :");
    String head = colon >= 0 ? s.substring(0, colon).trim() : s;

    String[] toks = head.split("\\s+");
    if (toks.length < 7) return null;
    if (!"354".equals(toks[0])) return null;

    String token = toks[2];
    if (token.isBlank()) return null;
    if (expectedToken != null && !expectedToken.isBlank() && !expectedToken.equals(token))
      return null;

    String channel = toks[3];
    String user = toks[4];
    String host = toks[5];
    String nick = toks[6];

    String f1 = toks.length > 7 ? toks[7] : null;
    String f2 = toks.length > 8 ? toks[8] : null;

    String flags = null;
    String account = null;
    if (f2 == null) {
      if (looksLikeWhoxFlags(f1)) flags = f1;
      else account = f1;
    } else {
      boolean f1Flags = looksLikeWhoxFlags(f1);
      boolean f2Flags = looksLikeWhoxFlags(f2);
      boolean f1Acct = looksLikeAccountToken(f1);
      boolean f2Acct = looksLikeAccountToken(f2);
      if (f1Flags && f2Acct && !f2Flags) {
        flags = f1;
        account = f2;
      } else if (f2Flags && f1Acct && !f1Flags) {
        flags = f2;
        account = f1;
      } else {
        flags = f1;
        account = f2;
      }
    }

    if (channel == null || channel.isBlank() || !looksLikeChannel(channel))
      return null;
    if (nick == null || nick.isBlank() || !looksLikeNick(nick)) return null;
    if (user == null || user.isBlank() || !looksLikeUser(user)) return null;
    if (host == null || host.isBlank() || !looksLikeHost(host)) return null;
    if (account != null && account.isBlank()) account = null;

    String hm = nick + "!" + user + "@" + host;
    if (!isUsefulHostmask(hm)) return null;

    if (flags == null) flags = "";

    return new ParsedWhoxTcuhnaf(token, channel, user, host, nick, flags, account);
  }

  /**
   * Returns true if this line appears to be an RPL_WHOSPCRPL (354) WHOX reply for the given token.
   *
   * <p>This is used to detect schema mismatches when strict parsing fails, so enrichment can fall
   * back to plain WHO/USERHOST instead of silently "working" without producing account updates.
   */
  public static boolean seemsRpl354WhoxWithToken(String line, String expectedToken) {
    if (line == null || expectedToken == null || expectedToken.isBlank()) return false;
    String s = line.trim();
    if (s.isEmpty()) return false;
    if (s.startsWith(":")) {
      int sp = s.indexOf(' ');
      if (sp > 0 && sp + 1 < s.length()) s = s.substring(sp + 1).trim();
    }

    int colon = s.indexOf(" :");
    String head = colon >= 0 ? s.substring(0, colon).trim() : s;
    String[] toks = head.split("\\s+");
    if (toks.length < 4) return false;
    if (!"354".equals(toks[0])) return false;
    if (!expectedToken.equals(toks[2])) return false;
    return looksLikeChannel(toks[3]);
  }

  private static boolean looksLikeWhoxFlags(String s) {
    if (s == null || s.isBlank()) return false;
    if (s.length() > 32) return false;
    return s.indexOf('H') >= 0
        || s.indexOf('G') >= 0
        || s.indexOf('@') >= 0
        || s.indexOf('+') >= 0
        || s.indexOf('%') >= 0
        || s.indexOf('~') >= 0
        || s.indexOf('&') >= 0
        || "*".equals(s);
  }

  private static boolean looksLikeAccountToken(String s) {
    if (s == null || s.isBlank()) return false;
    if ("*".equals(s) || "0".equals(s)) return true;
    if (looksLikeWhoxFlags(s) && s.length() <= 3) return false;
    return s.matches("[A-Za-z0-9_\\-\\.\\[\\]\\\\`\\^\\{\\|\\}]+");
  }

  /** Parse RPL_WHOSPCRPL (354) / WHOX lines. */
  public static ParsedWhoxReply parseRpl354WhoxReply(String line) {
    if (line == null) return null;
    String s = line.trim();
    if (s.isEmpty()) return null;
    if (s.startsWith(":")) {
      int sp = s.indexOf(' ');
      if (sp > 0 && sp + 1 < s.length()) s = s.substring(sp + 1).trim();
    }
    int colon = s.indexOf(" :");
    String head = colon >= 0 ? s.substring(0, colon).trim() : s;

    String[] toks = head.split("\\s+");
    if (toks.length < 3) return null;
    if (!"354".equals(toks[0])) return null;
    List<String> fields = new ArrayList<>();
    for (int i = 2; i < toks.length; i++) {
      String t = toks[i];
      if (t == null || t.isBlank()) continue;
      fields.add(t);
    }
    if (fields.isEmpty()) return null;
    int idx = 0;
    if (looksNumeric(fields.get(0))) idx++;

    String channel = "";
    if (idx < fields.size() && looksLikeChannel(fields.get(idx))) {
      channel = fields.get(idx);
    } else {
      for (String f : fields) {
        if (looksLikeChannel(f)) {
          channel = f;
          break;
        }
      }
    }
    int userIdx = -1;
    int hostIdx = -1;
    for (int i = 0; i < fields.size(); i++) {
      String a = fields.get(i);
      if (!looksLikeUser(a)) continue;

      if (i + 1 < fields.size()) {
        String b = fields.get(i + 1);
        if (looksLikeHost(b)
            && !looksLikeChannel(b)
            && !looksNumeric(b)) {
          userIdx = i;
          hostIdx = i + 1;
          break;
        }
      }
      if (i + 2 < fields.size()) {
        String b = fields.get(i + 1);
        String c = fields.get(i + 2);
        if (looksLikeIp(b)
            && looksLikeHost(c)
            && !looksLikeChannel(c)
            && !looksNumeric(c)) {
          userIdx = i;
          hostIdx = i + 2;
          break;
        }
      }
    }
    if (userIdx < 0 || hostIdx < 0) return null;

    String user = fields.get(userIdx);
    String host = fields.get(hostIdx);
    if (user == null || user.isBlank() || host == null || host.isBlank()) return null;
    String nick = null;
    for (int j = hostIdx + 1; j < fields.size(); j++) {
      String t = fields.get(j);
      if (t == null || t.isBlank()) continue;
      if (looksNumeric(t)) continue;
      if (looksLikeChannel(t)) continue;
      if (looksLikeHost(t) || looksLikeIp(t)) continue;
      if (!looksLikeNick(t)) continue;
      nick = t;
      break;
    }
    if (nick == null || nick.isBlank()) return null;

    String hm = nick + "!" + user + "@" + host;
    if (!isUsefulHostmask(hm)) return null;

    return new ParsedWhoxReply(channel, nick, user, host);
  }

  /**
   * Parse RPL_USERHOST (302) lines.
   *
   * <p>Format: ":server 302 <me> :nick[\*]=[+|-]user@host ..."
   */
  public static List<UserhostEntry> parseRpl302Userhost(String line) {
    if (line == null) return null;
    String s = line.trim();
    if (s.isEmpty()) return null;
    if (s.startsWith(":")) {
      int sp = s.indexOf(' ');
      if (sp > 0 && sp + 1 < s.length()) s = s.substring(sp + 1).trim();
    }
    String[] toks = s.split("\\s+");
    if (toks.length < 3) return null;
    if (!"302".equals(toks[0])) return null;

    int colon = s.indexOf(" :");
    if (colon < 0 || colon + 2 >= s.length()) return null;
    String payload = s.substring(colon + 2).trim();
    if (payload.isEmpty()) return null;

    List<UserhostEntry> out = new ArrayList<>();
    for (String part : payload.split("\\s+")) {
      if (part == null || part.isBlank()) continue;
      String p = part.trim();
      if (p.startsWith(":")) p = p.substring(1);

      int eq = p.indexOf('=');
      if (eq <= 0 || eq >= p.length() - 1) continue;

      String nickPart = p.substring(0, eq).trim();
      if (nickPart.endsWith("*")) nickPart = nickPart.substring(0, nickPart.length() - 1);
      String nick = nickPart.trim();
      if (nick.isEmpty()) continue;

      String rhs = p.substring(eq + 1).trim();
      if (rhs.isEmpty()) continue;
      AwayState as = AwayState.UNKNOWN;
      if (rhs.charAt(0) == '+' || rhs.charAt(0) == '-') {
        as = (rhs.charAt(0) == '-') ? AwayState.AWAY : AwayState.HERE;
        rhs = rhs.substring(1);
      }

      int at = rhs.indexOf('@');
      if (at <= 0 || at >= rhs.length() - 1) continue;
      String user = rhs.substring(0, at).trim();
      String host = rhs.substring(at + 1).trim();
      if (user.isEmpty() || host.isEmpty()) continue;

      String hm = nick + "!" + user + "@" + host;
      if (!isUsefulHostmask(hm)) continue;
      out.add(new UserhostEntry(nick, hm, as));
    }

    return out.isEmpty() ? null : List.copyOf(out);
  }

  private static boolean looksNumeric(String value) {
    if (value == null || value.isBlank()) return false;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c < '0' || c > '9') return false;
    }
    return true;
  }

  private static boolean looksLikeChannel(String value) {
    if (value == null || value.isBlank()) return false;
    char first = value.charAt(0);
    return first == '#' || first == '&';
  }

  private static boolean looksLikeUser(String value) {
    if (value == null || value.isBlank()) return false;
    if (looksLikeChannel(value) || looksNumeric(value)) return false;
    if (value.indexOf('!') >= 0 || value.indexOf('@') >= 0 || value.indexOf(':') >= 0) return false;
    return value.length() <= 64;
  }

  private static boolean looksLikeHost(String value) {
    if (value == null || value.isBlank()) return false;
    if (looksLikeChannel(value)) return false;
    if (value.indexOf('!') >= 0 || value.indexOf('@') >= 0) return false;
    return value.indexOf('.') >= 0 || value.indexOf(':') >= 0 || value.indexOf('/') >= 0;
  }

  private static boolean looksLikeIp(String value) {
    if (value == null || value.isBlank()) return false;
    if (value.matches("\\d{1,3}(?:\\.\\d{1,3}){3}")) return true;
    return value.indexOf(':') >= 0 && value.matches("[0-9A-Fa-f:]+");
  }

  private static boolean looksLikeNick(String value) {
    if (value == null || value.isBlank()) return false;
    if (looksLikeChannel(value) || looksNumeric(value)) return false;
    return value.matches("[A-Za-z\\[\\]\\\\`_\\^\\{\\|\\}][A-Za-z0-9\\-\\.\\[\\]\\\\`_\\^\\{\\|\\}]*");
  }

  private static boolean isUsefulHostmask(String hostmask) {
    if (hostmask == null) return false;
    String value = hostmask.trim();
    if (value.isEmpty()) return false;
    int bang = value.indexOf('!');
    int at = value.indexOf('@');
    if (bang <= 0 || at <= bang + 1 || at >= value.length() - 1) return false;
    String ident = value.substring(bang + 1, at).trim();
    String host = value.substring(at + 1).trim();
    return !(ident.isEmpty() || "*".equals(ident)) || !(host.isEmpty() || "*".equals(host));
  }
}
