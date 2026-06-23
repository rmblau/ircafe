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

  default List<SlashCommandDescriptor> autocompleteCommands() {
    return List.of();
  }

  default void appendGeneralHelp(SlashCommandHelpSink help) {}

  default Map<String, Consumer<SlashCommandHelpSink>> topicHelpHandlers() {
    return Map.of();
  }
}
