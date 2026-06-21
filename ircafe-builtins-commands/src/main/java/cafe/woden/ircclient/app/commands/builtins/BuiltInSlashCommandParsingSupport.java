package cafe.woden.ircclient.app.commands.builtins;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import java.util.ArrayList;
import java.util.List;

final class BuiltInSlashCommandParsingSupport {

  private BuiltInSlashCommandParsingSupport() {}

  static String argAfter(String line, String cmd) {
    if (line == null || cmd == null) return "";
    if (line.equalsIgnoreCase(cmd)) return "";
    if (line.length() <= cmd.length()) return "";
    String rest = line.substring(cmd.length());
    return rest.trim();
  }

  static boolean matchesCommand(String line, String cmd) {
    if (line == null || cmd == null) return false;
    if (line.length() < cmd.length()) return false;
    if (!line.regionMatches(true, 0, cmd, 0, cmd.length())) return false;
    if (line.length() == cmd.length()) return true;
    char next = line.charAt(cmd.length());
    return Character.isWhitespace(next);
  }

  static boolean looksLikePartTarget(String token) {
    String value = token == null ? "" : token.trim();
    if (value.isEmpty()) return false;
    return value.startsWith("#") || value.startsWith("&");
  }

  static SlashCommandParseResult parseJoinInput(String rest) {
    String r = rest == null ? "" : rest.trim();
    if (r.isEmpty()) return SlashCommandParseResult.join("", "");

    String[] toks = r.split("\\s+", 3);
    if (toks.length == 0) return SlashCommandParseResult.join("", "");
    String first = toks[0].trim();
    if (isJoinInviteOption(first)) {
      if (toks.length == 1) return SlashCommandParseResult.command("invite-join", "last");
      if (toks.length == 2) return SlashCommandParseResult.command("invite-join", toks[1].trim());
      return SlashCommandParseResult.command("invite-join", (toks[1] + " " + toks[2]).trim());
    }
    if (toks.length > 2) return SlashCommandParseResult.join("", "");

    String channel = first;
    String key = toks.length > 1 ? toks[1].trim() : "";
    return SlashCommandParseResult.join(channel, key);
  }

  private static boolean isJoinInviteOption(String token) {
    if (token == null) return false;
    return "-invite".equalsIgnoreCase(token.trim()) || "-i".equalsIgnoreCase(token.trim());
  }

  static SlashCommandParseResult parseWhowasInput(String rest) {
    String r = rest == null ? "" : rest.trim();
    if (r.isEmpty()) return SlashCommandParseResult.command("whowas", "", "0");

    String[] toks = r.split("\\s+", 3);
    if (toks.length == 0) return SlashCommandParseResult.command("whowas", "", "0");
    if (toks.length > 2) return SlashCommandParseResult.command("whowas", "", "0");

    String nick = toks[0].trim();
    if (nick.isEmpty()) return SlashCommandParseResult.command("whowas", "", "0");

    if (toks.length == 1) return SlashCommandParseResult.command("whowas", nick, "0");

    String countRaw = toks[1].trim();
    if (!isIntegerToken(countRaw)) return SlashCommandParseResult.command("whowas", "", "0");
    int count = parseIntOrZero(countRaw);
    if (count < 0) return SlashCommandParseResult.command("whowas", "", "0");
    return SlashCommandParseResult.command("whowas", nick, Integer.toString(count));
  }

  static ParsedKick parseKickArgs(String rest) {
    String r = rest == null ? "" : rest.trim();
    if (r.isEmpty()) return new ParsedKick("", "", "");

    String first;
    String afterFirst;
    int sp = r.indexOf(' ');
    if (sp < 0) {
      first = r;
      afterFirst = "";
    } else {
      first = r.substring(0, sp).trim();
      afterFirst = r.substring(sp + 1).trim();
    }

    if (first.startsWith("#") || first.startsWith("&")) {
      if (afterFirst.isEmpty()) return new ParsedKick(first, "", "");
      int sp2 = afterFirst.indexOf(' ');
      if (sp2 < 0) return new ParsedKick(first, afterFirst.trim(), "");
      String nick = afterFirst.substring(0, sp2).trim();
      String reason = afterFirst.substring(sp2 + 1).trim();
      return new ParsedKick(first, nick, reason);
    }

    return new ParsedKick("", first, afterFirst);
  }

  static ParsedTargetList parseTargetList(String rest) {
    String r = rest == null ? "" : rest.trim();
    if (r.isEmpty()) return new ParsedTargetList("", List.of());

    String[] toks = r.split("\\s+");
    String channel = "";
    int idx = 0;
    if (toks.length > 0 && (toks[0].startsWith("#") || toks[0].startsWith("&"))) {
      channel = toks[0];
      idx = 1;
    }

    List<String> items = new ArrayList<>();
    for (int i = idx; i < toks.length; i++) {
      String t = toks[i].trim();
      if (!t.isEmpty()) items.add(t);
    }
    return new ParsedTargetList(channel, List.copyOf(items));
  }

  static boolean isIntegerToken(String raw) {
    String s = raw == null ? "" : raw.trim();
    if (s.isEmpty()) return false;
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      if (i == 0 && (ch == '+' || ch == '-')) {
        if (s.length() == 1) return false;
        continue;
      }
      if (ch < '0' || ch > '9') return false;
    }
    return true;
  }

  static int parseIntOrZero(String raw) {
    try {
      return Integer.parseInt(raw == null ? "" : raw.trim());
    } catch (Exception ignored) {
      return 0;
    }
  }

  record ParsedTargetList(String channel, List<String> items) {}

  record ParsedKick(String channel, String nick, String reason) {}
}
