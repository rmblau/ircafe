package cafe.woden.ircclient.app.commands.spi;

import java.util.Locale;
import java.util.Objects;

/** Portable parsed representation of a backend-scoped named slash command. */
public record BackendNamedCommandParseResult(String command, String args) {

  public BackendNamedCommandParseResult {
    command = normalizeCommand(command);
    args = Objects.toString(args, "").trim();
  }

  private static String normalizeCommand(String raw) {
    String command = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    if (command.startsWith("/")) {
      command = command.substring(1).trim();
    }
    return command;
  }
}
