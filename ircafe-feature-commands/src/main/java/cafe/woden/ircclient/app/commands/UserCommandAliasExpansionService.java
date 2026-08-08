package cafe.woden.ircclient.app.commands;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Feature-owned expansion engine for user-defined slash-command aliases. */
public final class UserCommandAliasExpansionService {

  private static final int MAX_EXPANSION_DEPTH = 8;

  public UserCommandAliasExpansionResult expand(
      String raw,
      List<UserCommandAliasDefinition> aliases,
      UserCommandAliasExpansionContext context) {
    String line = Objects.toString(raw, "").trim();
    if (line.isEmpty()) return new UserCommandAliasExpansionResult(List.of(), List.of());

    Map<String, UserCommandAliasDefinition> aliasMap = buildAliasMap(aliases);
    if (aliasMap.isEmpty()) {
      return new UserCommandAliasExpansionResult(List.of(line), List.of());
    }

    UserCommandAliasExpansionContext ctx =
        Objects.requireNonNullElse(context, UserCommandAliasExpansionContext.empty());
    List<String> out = new ArrayList<>();
    LinkedHashSet<String> warnings = new LinkedHashSet<>();
    expandLine(line, ctx, aliasMap, 0, new ArrayDeque<>(), out, warnings);

    if (out.isEmpty()) {
      return new UserCommandAliasExpansionResult(List.of(), List.copyOf(warnings));
    }
    return new UserCommandAliasExpansionResult(List.copyOf(out), List.copyOf(warnings));
  }

  private void expandLine(
      String line,
      UserCommandAliasExpansionContext ctx,
      Map<String, UserCommandAliasDefinition> aliases,
      int depth,
      Deque<String> stack,
      List<String> out,
      LinkedHashSet<String> warnings) {
    String s = Objects.toString(line, "").trim();
    if (s.isEmpty()) return;

    Invocation inv = parseInvocation(s);
    if (inv == null) {
      out.add(s);
      return;
    }

    String key = normalizeCommandName(inv.command());
    if (key == null) {
      out.add(s);
      return;
    }

    UserCommandAliasDefinition alias = aliases.get(key);
    if (alias == null) {
      out.add(s);
      return;
    }

    if (depth >= MAX_EXPANSION_DEPTH) {
      warnings.add("Alias expansion depth exceeded for /" + key + ".");
      return;
    }

    if (stack.contains(key)) {
      warnings.add("Alias recursion detected for /" + key + ".");
      return;
    }

    String expanded = expandTemplate(alias.template(), inv.args(), ctx);
    List<String> pieces = splitExpandedCommands(expanded);
    if (pieces.isEmpty()) return;

    stack.addLast(key);
    try {
      for (String piece : pieces) {
        expandLine(piece, ctx, aliases, depth + 1, stack, out, warnings);
      }
    } finally {
      if (!stack.isEmpty()) stack.removeLast();
    }
  }

  private static Map<String, UserCommandAliasDefinition> buildAliasMap(
      List<UserCommandAliasDefinition> aliases) {
    Map<String, UserCommandAliasDefinition> out = new LinkedHashMap<>();
    if (aliases == null || aliases.isEmpty()) return out;

    for (UserCommandAliasDefinition alias : aliases) {
      if (alias == null || !alias.enabled()) continue;
      String key = normalizeCommandName(alias.name());
      if (key == null) continue;
      out.putIfAbsent(key, alias);
    }

    return out;
  }

  private static Invocation parseInvocation(String line) {
    String s = Objects.toString(line, "").trim();
    if (s.isEmpty() || !s.startsWith("/")) return null;
    if (s.startsWith("//")) return null;

    String rest = s.substring(1).trim();
    if (rest.isEmpty()) return null;

    int sp = rest.indexOf(' ');
    if (sp < 0) {
      return new Invocation(rest, "");
    }

    String cmd = rest.substring(0, sp).trim();
    String args = rest.substring(sp + 1).trim();
    return new Invocation(cmd, args);
  }

  private static String expandTemplate(
      String template, String rawArgs, UserCommandAliasExpansionContext ctx) {
    String base = Objects.toString(template, "");
    String args = Objects.toString(rawArgs, "").trim();

    List<String> tokens = splitArgs(args);

    String out = base;

    out = out.replace("%*", args).replace("$*", args);

    for (int i = 9; i >= 1; i--) {
      String fromN = joinFrom(tokens, i);
      out = out.replace("%" + i + "-", fromN);
      out = out.replace("$" + i + "-", fromN);
    }

    for (int i = 9; i >= 1; i--) {
      String nth = tokenAt(tokens, i);
      out = out.replace("%" + i, nth);
      out = out.replace("$" + i, nth);
    }

    for (int i = 9; i >= 1; i--) {
      String fromEnd = tokenFromEnd(tokens, i);
      out = out.replace("&" + i, fromEnd);
    }

    String target = Objects.toString(ctx.target(), "");
    String channel = Objects.toString(ctx.channel(), "");
    if (channel.isBlank()) channel = target;

    out = replaceIgnoreCase(out, "%c", channel);
    out = replaceIgnoreCase(out, "%t", target);
    out = replaceIgnoreCase(out, "%s", Objects.toString(ctx.serverId(), ""));
    out = replaceIgnoreCase(out, "%e", Objects.toString(ctx.serverId(), ""));
    out = replaceIgnoreCase(out, "%n", Objects.toString(ctx.nick(), ""));
    out = replaceIgnoreCase(out, "%hexchat_time", Objects.toString(ctx.hexChatTime(), ""));
    out = replaceIgnoreCase(out, "%hexchat_version", Objects.toString(ctx.hexChatVersion(), ""));
    out = replaceIgnoreCase(out, "%hexchat_machine", Objects.toString(ctx.hexChatMachine(), ""));

    // Replace longer aliases first to avoid partial substitution ($nick before $n).
    out = replaceIgnoreCase(out, "$nick", Objects.toString(ctx.nick(), ""));
    out = replaceIgnoreCase(out, "$me", Objects.toString(ctx.nick(), ""));
    out = replaceIgnoreCase(out, "$c", channel);
    out = replaceIgnoreCase(out, "$t", target);
    out = replaceIgnoreCase(out, "$s", Objects.toString(ctx.serverId(), ""));
    out = replaceIgnoreCase(out, "$n", Objects.toString(ctx.nick(), ""));

    out = out.replace("%%", "%").replace("$$", "$");
    return out;
  }

  private static List<String> splitArgs(String rawArgs) {
    String s = Objects.toString(rawArgs, "").trim();
    if (s.isEmpty()) return List.of();

    String[] parts = s.split("\\s+");
    List<String> out = new ArrayList<>(parts.length);
    for (String p : parts) {
      String v = Objects.toString(p, "").trim();
      if (!v.isEmpty()) out.add(v);
    }
    return out;
  }

  private static String tokenAt(List<String> tokens, int oneBased) {
    int idx = oneBased - 1;
    if (tokens == null || idx < 0 || idx >= tokens.size()) return "";
    return Objects.toString(tokens.get(idx), "");
  }

  private static String joinFrom(List<String> tokens, int oneBased) {
    int idx = oneBased - 1;
    if (tokens == null || idx < 0 || idx >= tokens.size()) return "";
    return String.join(" ", tokens.subList(idx, tokens.size()));
  }

  private static String tokenFromEnd(List<String> tokens, int oneBased) {
    int idx = oneBased - 1;
    if (tokens == null || idx < 0 || idx >= tokens.size()) return "";
    int fromEnd = tokens.size() - 1 - idx;
    if (fromEnd < 0 || fromEnd >= tokens.size()) return "";
    return Objects.toString(tokens.get(fromEnd), "");
  }

  private static String replaceIgnoreCase(String in, String token, String replacement) {
    String src = Objects.toString(in, "");
    String tk = Objects.toString(token, "");
    if (tk.isEmpty() || src.isEmpty()) return src;

    String repl = Objects.toString(replacement, "");
    String lowerSrc = src.toLowerCase(Locale.ROOT);
    String lowerTk = tk.toLowerCase(Locale.ROOT);

    StringBuilder out = new StringBuilder(src.length() + 16);
    int from = 0;
    while (true) {
      int at = lowerSrc.indexOf(lowerTk, from);
      if (at < 0) break;
      out.append(src, from, at);
      out.append(repl);
      from = at + tk.length();
    }
    out.append(src.substring(from));
    return out.toString();
  }

  public static List<String> splitExpandedCommands(String expanded) {
    String s = Objects.toString(expanded, "");
    if (s.isBlank()) return List.of();

    List<String> out = new ArrayList<>();
    StringBuilder cur = new StringBuilder();

    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);

      if (ch == '\\' && i + 1 < s.length()) {
        char next = s.charAt(i + 1);
        if (next == ';' || next == '\\') {
          cur.append(next);
          i++;
          continue;
        }
        if (next == 'n') {
          appendCommandToken(out, cur);
          i++;
          continue;
        }
      }

      if (ch == ';' || ch == '\n' || ch == '\r') {
        appendCommandToken(out, cur);
        continue;
      }

      cur.append(ch);
    }

    appendCommandToken(out, cur);
    return out;
  }

  private static void appendCommandToken(List<String> out, StringBuilder cur) {
    String token = cur.toString().trim();
    cur.setLength(0);
    if (!token.isEmpty()) out.add(token);
  }

  private static String normalizeCommandName(String rawName) {
    String n = norm(rawName);
    if (n.startsWith("/")) n = n.substring(1).trim();
    if (n.isEmpty()) return null;
    int sp = n.indexOf(' ');
    if (sp >= 0) n = n.substring(0, sp).trim();
    n = n.toLowerCase(Locale.ROOT);
    return n.isEmpty() ? null : n;
  }

  private static String norm(String value) {
    return Objects.toString(value, "").trim();
  }

  private record Invocation(String command, String args) {}
}
