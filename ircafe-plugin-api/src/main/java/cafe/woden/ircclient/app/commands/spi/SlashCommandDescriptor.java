package cafe.woden.ircclient.app.commands.spi;

import java.util.Locale;
import java.util.Objects;

/** Presentation metadata for a slash command exposed in autocomplete/help surfaces. */
public record SlashCommandDescriptor(String command, String summary) {

  public SlashCommandDescriptor {
    String normalized = Objects.toString(command, "").trim();
    if (!normalized.startsWith("/")) {
      normalized = "/" + normalized;
    }
    command = normalized.toLowerCase(Locale.ROOT);
    summary = Objects.toString(summary, "").trim();
  }
}
