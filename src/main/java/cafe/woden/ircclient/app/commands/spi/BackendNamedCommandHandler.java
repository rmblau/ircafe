package cafe.woden.ircclient.app.commands.spi;

import cafe.woden.ircclient.app.commands.ParsedInput;
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

  Set<String> supportedCommandNames();

  ParsedInput parse(String line, String matchedCommandName);

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
