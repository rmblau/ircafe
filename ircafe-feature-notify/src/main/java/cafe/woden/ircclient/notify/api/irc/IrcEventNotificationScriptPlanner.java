package cafe.woden.ircclient.notify.api.irc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Feature-owned planning for IRC event notification script execution. */
public final class IrcEventNotificationScriptPlanner {
  private IrcEventNotificationScriptPlanner() {}

  public static IrcEventNotificationScriptPlan plan(
      String scriptPath,
      String scriptArgs,
      String scriptWorkingDirectory,
      String eventType,
      String serverId,
      String channel,
      String sourceNick,
      Boolean sourceIsSelf,
      String title,
      String body,
      String ctcpCommand,
      String ctcpValue,
      long timestampMs) {
    String script = normalizeToNull(scriptPath);
    if (script == null) {
      return new IrcEventNotificationScriptPlan(List.of(), null, Map.of());
    }

    List<String> command = new ArrayList<>();
    command.add(script);
    command.addAll(parseCommandArgs(scriptArgs));

    Map<String, String> env = new LinkedHashMap<>();
    putEnv(env, "IRCAFE_EVENT_TYPE", eventType);
    putEnv(env, "IRCAFE_SERVER_ID", serverId);
    putEnv(env, "IRCAFE_CHANNEL", channel);
    putEnv(env, "IRCAFE_SOURCE_NICK", sourceNick);
    putEnv(
        env,
        "IRCAFE_SOURCE_IS_SELF",
        sourceIsSelf == null ? "unknown" : Boolean.toString(sourceIsSelf));
    putEnv(env, "IRCAFE_TITLE", title);
    putEnv(env, "IRCAFE_BODY", body);
    putEnv(env, "IRCAFE_CTCP_COMMAND", ctcpCommand);
    putEnv(env, "IRCAFE_CTCP_VALUE", ctcpValue);
    putEnv(env, "IRCAFE_TIMESTAMP_MS", Long.toString(timestampMs));

    return new IrcEventNotificationScriptPlan(
        command, normalizeToNull(scriptWorkingDirectory), env);
  }

  public static List<String> parseCommandArgs(String rawArgs) {
    String input = normalizeToNull(rawArgs);
    if (input == null) return List.of();

    List<String> out = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inSingle = false;
    boolean inDouble = false;
    boolean escaping = false;

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      if (escaping) {
        current.append(c);
        escaping = false;
        continue;
      }

      if (c == '\\') {
        escaping = true;
        continue;
      }

      if (c == '\'' && !inDouble) {
        inSingle = !inSingle;
        continue;
      }

      if (c == '"' && !inSingle) {
        inDouble = !inDouble;
        continue;
      }

      if (Character.isWhitespace(c) && !inSingle && !inDouble) {
        appendToken(out, current);
        continue;
      }

      current.append(c);
    }

    if (escaping) current.append('\\');
    if (inSingle || inDouble) {
      throw new IllegalArgumentException("Unterminated quoted script arguments.");
    }
    appendToken(out, current);
    return List.copyOf(out);
  }

  private static void appendToken(List<String> out, StringBuilder current) {
    if (current == null || current.isEmpty()) return;
    out.add(current.toString());
    current.setLength(0);
  }

  private static void putEnv(Map<String, String> env, String key, String value) {
    if (env == null || key == null || key.isBlank()) return;
    env.put(key, Objects.toString(value, ""));
  }

  private static String normalizeToNull(String raw) {
    String value = Objects.toString(raw, "").trim();
    return value.isEmpty() ? null : value;
  }
}
