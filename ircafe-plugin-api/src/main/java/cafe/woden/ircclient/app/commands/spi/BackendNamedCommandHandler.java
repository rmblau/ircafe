package cafe.woden.ircclient.app.commands.spi;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ServiceLoader-backed metadata and parser contribution for backend-scoped named commands.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler}.
 */
public interface BackendNamedCommandHandler {

  /** Returns command names without a leading slash. Matching is case-insensitive. */
  Set<String> supportedCommandNames();

  /**
   * Parses a line already matched to one of {@link #supportedCommandNames()}. The returned command
   * should be the canonical executor-facing name; aliases may therefore map to one command.
   */
  BackendNamedCommandParseResult parse(String line, String matchedCommandName);

  default List<SlashCommandDescriptor> autocompleteCommands() {
    return List.of();
  }

  default List<String> generalHelpLines() {
    return List.of();
  }

  default Map<String, List<String>> topicHelpLines() {
    return Map.of();
  }
}
