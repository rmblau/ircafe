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


  static SlashCommandParseResult parseDccInput(String rest) {
    String r = rest == null ? "" : rest.trim();
    if (r.isEmpty()) return SlashCommandParseResult.command("dcc", "", "", "");

    int sp1 = r.indexOf(' ');
    if (sp1 < 0) {
      return SlashCommandParseResult.command("dcc", r, "", "");
    }

    String sub = r.substring(0, sp1).trim();
    String rest2 = r.substring(sp1 + 1).trim();
    if (rest2.isEmpty()) {
      return SlashCommandParseResult.command("dcc", sub, "", "");
    }

    int sp2 = rest2.indexOf(' ');
    if (sp2 < 0) {
      return SlashCommandParseResult.command("dcc", sub, rest2.trim(), "");
    }

    String nick = rest2.substring(0, sp2).trim();
    String arg = rest2.substring(sp2 + 1).trim();
    return SlashCommandParseResult.command("dcc", sub, nick, arg);
  }

  static SlashCommandParseResult parseUploadInput(String rest) {
    String raw = rest == null ? "" : rest.trim();
    if (raw.isEmpty()) return SlashCommandParseResult.command("upload", "", "", "");

    int firstSpace = raw.indexOf(' ');
    if (firstSpace <= 0) {
      return SlashCommandParseResult.command("upload", raw.trim(), "", "");
    }

    String msgType = raw.substring(0, firstSpace).trim();
    String remaining = raw.substring(firstSpace + 1).trim();
    if (remaining.isEmpty()) {
      return SlashCommandParseResult.command("upload", msgType, "", "");
    }

    ParsedPathToken pathToken = parsePathToken(remaining);
    return SlashCommandParseResult.command(
        "upload", msgType, pathToken.path(), pathToken.remainder() == null ? "" : pathToken.remainder());
  }

  private static ParsedPathToken parsePathToken(String raw) {
    String input = raw == null ? "" : raw.trim();
    if (input.isEmpty()) {
      return new ParsedPathToken("", "");
    }

    if (!input.startsWith("\"")) {
      int sp = input.indexOf(' ');
      if (sp < 0) {
        return new ParsedPathToken(input, "");
      }
      return new ParsedPathToken(input.substring(0, sp).trim(), input.substring(sp + 1).trim());
    }

    StringBuilder token = new StringBuilder();
    boolean escaped = false;
    for (int i = 1; i < input.length(); i++) {
      char ch = input.charAt(i);
      if (escaped) {
        token.append(ch);
        escaped = false;
        continue;
      }
      if (ch == '\\') {
        escaped = true;
        continue;
      }
      if (ch == '"') {
        String remainder = i + 1 < input.length() ? input.substring(i + 1).trim() : "";
        return new ParsedPathToken(token.toString(), remainder);
      }
      token.append(ch);
    }

    return new ParsedPathToken("", "");
  }

  static SlashCommandParseResult parseReplyInput(String rest) {
    String r = rest == null ? "" : rest.trim();
    if (r.isEmpty()) return SlashCommandParseResult.command("reply", "", "");
    int sp = r.indexOf(' ');
    if (sp <= 0) return SlashCommandParseResult.command("reply", r.trim(), "");
    String msgId = r.substring(0, sp).trim();
    String body = r.substring(sp + 1).trim();
    return SlashCommandParseResult.command("reply", msgId, body);
  }

  static SlashCommandParseResult parseReactInput(String rest) {
    String r = rest == null ? "" : rest.trim();
    if (r.isEmpty()) return SlashCommandParseResult.command("react", "", "");
    int sp = r.indexOf(' ');
    if (sp <= 0) return SlashCommandParseResult.command("react", r.trim(), "");
    String msgId = r.substring(0, sp).trim();
    String reaction = r.substring(sp + 1).trim();
    return SlashCommandParseResult.command("react", msgId, reaction);
  }

  static SlashCommandParseResult parseUnreactInput(String rest) {
    String r = rest == null ? "" : rest.trim();
    if (r.isEmpty()) return SlashCommandParseResult.command("unreact", "", "");
    int sp = r.indexOf(' ');
    if (sp <= 0) return SlashCommandParseResult.command("unreact", r.trim(), "");
    String msgId = r.substring(0, sp).trim();
    String reaction = r.substring(sp + 1).trim();
    return SlashCommandParseResult.command("unreact", msgId, reaction);
  }

  static SlashCommandParseResult parseEditInput(String rest) {
    String r = rest == null ? "" : rest.trim();
    if (r.isEmpty()) return SlashCommandParseResult.command("edit", "", "");
    int sp = r.indexOf(' ');
    if (sp <= 0) return SlashCommandParseResult.command("edit", r.trim(), "");
    String msgId = r.substring(0, sp).trim();
    String body = r.substring(sp + 1).trim();
    return SlashCommandParseResult.command("edit", msgId, body);
  }

  static SlashCommandParseResult parseRedactInput(String rest) {
    String r = rest == null ? "" : rest.trim();
    if (r.isEmpty()) return SlashCommandParseResult.command("redact", "", "");
    int sp = r.indexOf(' ');
    if (sp < 0) {
      return SlashCommandParseResult.command("redact", r, "");
    }
    String msgId = r.substring(0, sp).trim();
    String reason = r.substring(sp + 1).trim();
    return SlashCommandParseResult.command("redact", msgId, reason);
  }

  static SlashCommandParseResult parseChatHistoryInput(String rest) {
    String r = rest == null ? "" : rest.trim();
    if (r.isEmpty()) {
      return SlashCommandParseResult.command("chat-history-before", "0", "");
    }

    String[] toks = r.split("\\s+");
    if (toks.length == 0) {
      return SlashCommandParseResult.command("chat-history-before", "0", "");
    }

    String head = toks[0].toLowerCase(java.util.Locale.ROOT);
    return switch (head) {
      case "before" -> parseChatHistoryBefore(toks, 1);
      case "latest" -> parseChatHistoryLatest(toks, 1);
      case "between" -> parseChatHistoryBetween(toks, 1);
      case "around" -> parseChatHistoryAround(toks, 1);
      default -> parseChatHistoryBefore(toks, 0);
    };
  }

  private static SlashCommandParseResult parseChatHistoryBefore(String[] toks, int startIdx) {
    if (toks == null || startIdx >= toks.length) {
      return SlashCommandParseResult.command("chat-history-before", "0", "");
    }

    int idx = startIdx;
    int lim = 50;
    String selector = "";
    String first = toks[idx];
    if (isIntegerToken(first)) {
      lim = parseIntOrZero(first);
      idx++;
    } else {
      selector = normalizeChatHistorySelector(first);
      idx++;
      if (selector.isEmpty()) {
        return SlashCommandParseResult.command("chat-history-before", "0", "");
      }
    }

    if (idx < toks.length) {
      if (!isIntegerToken(toks[idx])) return SlashCommandParseResult.command("chat-history-before", "0", "");
      lim = parseIntOrZero(toks[idx]);
      idx++;
    }
    if (idx < toks.length) {
      return SlashCommandParseResult.command("chat-history-before", "0", "");
    }
    return SlashCommandParseResult.command("chat-history-before", Integer.toString(lim), selector);
  }

  private static SlashCommandParseResult parseChatHistoryLatest(String[] toks, int startIdx) {
    int idx = startIdx;
    int lim = 50;
    String selector = "*";

    if (toks == null) return SlashCommandParseResult.command("chat-history-latest", "0", selector);

    if (idx < toks.length) {
      String first = toks[idx];
      if (isIntegerToken(first)) {
        lim = parseIntOrZero(first);
        idx++;
      } else {
        selector = normalizeChatHistorySelectorOrWildcard(first);
        idx++;
        if (selector.isEmpty()) {
          return SlashCommandParseResult.command("chat-history-latest", "0", "");
        }
      }
    }

    if (idx < toks.length) {
      if (!isIntegerToken(toks[idx])) return SlashCommandParseResult.command("chat-history-latest", "0", "");
      lim = parseIntOrZero(toks[idx]);
      idx++;
    }
    if (idx < toks.length) return SlashCommandParseResult.command("chat-history-latest", "0", "");

    return SlashCommandParseResult.command("chat-history-latest", Integer.toString(lim), selector);
  }

  private static SlashCommandParseResult parseChatHistoryAround(String[] toks, int startIdx) {
    if (toks == null || startIdx >= toks.length) return SlashCommandParseResult.command("chat-history-around", "", "0");

    int idx = startIdx;
    String selector = normalizeChatHistorySelector(toks[idx]);
    if (selector.isEmpty()) return SlashCommandParseResult.command("chat-history-around", "", "0");
    idx++;

    int lim = 50;
    if (idx < toks.length) {
      if (!isIntegerToken(toks[idx])) return SlashCommandParseResult.command("chat-history-around", "", "0");
      lim = parseIntOrZero(toks[idx]);
      idx++;
    }
    if (idx < toks.length) return SlashCommandParseResult.command("chat-history-around", "", "0");

    return SlashCommandParseResult.command("chat-history-around", selector, Integer.toString(lim));
  }

  private static SlashCommandParseResult parseChatHistoryBetween(String[] toks, int startIdx) {
    if (toks == null || startIdx + 1 >= toks.length) {
      return SlashCommandParseResult.command("chat-history-between", "", "", "0");
    }

    int idx = startIdx;
    String startSelector = normalizeChatHistorySelectorOrWildcard(toks[idx++]);
    String endSelector = normalizeChatHistorySelectorOrWildcard(toks[idx++]);
    if (startSelector.isEmpty() || endSelector.isEmpty()) {
      return SlashCommandParseResult.command("chat-history-between", "", "", "0");
    }

    int lim = 50;
    if (idx < toks.length) {
      if (!isIntegerToken(toks[idx])) return SlashCommandParseResult.command("chat-history-between", "", "", "0");
      lim = parseIntOrZero(toks[idx]);
      idx++;
    }
    if (idx < toks.length) return SlashCommandParseResult.command("chat-history-between", "", "", "0");

    return SlashCommandParseResult.command(
        "chat-history-between", startSelector, endSelector, Integer.toString(lim));
  }

  private static String normalizeChatHistorySelector(String raw) {
    String s = raw == null ? "" : raw.trim();
    if (s.isEmpty()) return "";
    int eq = s.indexOf('=');
    if (eq <= 0 || eq == s.length() - 1) return "";
    String key = s.substring(0, eq).trim().toLowerCase(java.util.Locale.ROOT);
    String value = s.substring(eq + 1).trim();
    if (value.isEmpty()) return "";
    if (!"timestamp".equals(key) && !"msgid".equals(key)) return "";
    return key + "=" + value;
  }

  private static String normalizeChatHistorySelectorOrWildcard(String raw) {
    String s = raw == null ? "" : raw.trim();
    if ("*".equals(s)) return "*";
    return normalizeChatHistorySelector(s);
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

  private record ParsedPathToken(String path, String remainder) {}
}
