package cafe.woden.ircclient.app.commands;

import java.util.Objects;

/** Feature-owned helpers for matching and slicing slash-command lines. */
public final class SlashCommandLineSupport {

  private SlashCommandLineSupport() {}

  public static boolean matchesCommand(String line, String command) {
    if (line == null || command == null) return false;
    if (line.length() < command.length()) return false;
    if (!line.regionMatches(true, 0, command, 0, command.length())) return false;
    if (line.length() == command.length()) return true;
    char next = line.charAt(command.length());
    return Character.isWhitespace(next);
  }

  public static String argAfter(String line, String command) {
    String raw = Objects.toString(line, "");
    String cmd = Objects.toString(command, "");
    if (raw.isEmpty() || cmd.isEmpty()) return "";
    if (raw.equalsIgnoreCase(cmd)) return "";
    if (raw.length() <= cmd.length()) return "";
    return raw.substring(cmd.length()).trim();
  }
}
