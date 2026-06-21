package cafe.woden.ircclient.app.commands.spi;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Portable result produced by plugin slash-command parse strategies. */
public record SlashCommandParseResult(String kind, List<String> arguments) {

  public SlashCommandParseResult {
    kind = normalizeKind(kind);
    arguments = List.copyOf(Objects.requireNonNullElse(arguments, List.of()));
  }

  public static SlashCommandParseResult command(String kind, String... arguments) {
    return command(kind, Arrays.asList(Objects.requireNonNullElse(arguments, new String[0])));
  }

  public static SlashCommandParseResult command(String kind, List<String> arguments) {
    return new SlashCommandParseResult(kind, arguments);
  }

  public static SlashCommandParseResult join(String channel, String key) {
    return command("join", channel, key);
  }

  public static SlashCommandParseResult part(String channel, String reason) {
    return command("part", channel, reason);
  }

  public static SlashCommandParseResult backendNamed(String command, String args) {
    return command("backend-named", command, args);
  }

  public static SlashCommandParseResult quote(String rawLine) {
    return command("quote", rawLine);
  }

  public static SlashCommandParseResult say(String text) {
    return command("say", text);
  }

  public static SlashCommandParseResult unknown(String raw) {
    return command("unknown", raw);
  }

  private static String normalizeKind(String raw) {
    String normalized = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    if (normalized.startsWith("/")) {
      normalized = normalized.substring(1).trim();
    }
    return normalized.replace('_', '-');
  }
}
