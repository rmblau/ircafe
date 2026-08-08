package cafe.woden.ircclient.app.commands.spi;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * ServiceLoader-backed contribution point for slash-command autocomplete and help presentation
 * metadata.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor}.
 */
public interface SlashCommandPresentationContributor {

  /** Returns stateless autocomplete metadata; command tokens are normalized by the descriptor. */
  default List<SlashCommandDescriptor> autocompleteCommands() {
    return List.of();
  }

  /** Appends general help lines through the app-owned sink. */
  default void appendGeneralHelp(SlashCommandHelpSink help) {}

  /**
   * Returns topic handlers keyed by the command name without the leading slash. Implementations
   * should remain stateless and use only the supplied help sink.
   */
  default Map<String, Consumer<SlashCommandHelpSink>> topicHelpHandlers() {
    return Map.of();
  }
}
