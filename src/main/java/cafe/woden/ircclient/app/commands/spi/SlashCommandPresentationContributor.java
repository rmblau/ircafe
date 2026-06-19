package cafe.woden.ircclient.app.commands.spi;

import cafe.woden.ircclient.model.TargetRef;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jmolecules.architecture.layered.ApplicationLayer;

/**
 * ServiceLoader-backed contribution point for slash-command autocomplete and help presentation
 * metadata.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor}.
 */
@ApplicationLayer
public interface SlashCommandPresentationContributor {

  default List<SlashCommandDescriptor> autocompleteCommands() {
    return List.of();
  }

  default void appendGeneralHelp(TargetRef out) {}

  default void appendGeneralHelp(TargetRef out, BiConsumer<TargetRef, String> lineAppender) {
    appendGeneralHelp(out);
  }

  default Map<String, Consumer<TargetRef>> topicHelpHandlers() {
    return Map.of();
  }

  default Map<String, Consumer<TargetRef>> topicHelpHandlers(
      BiConsumer<TargetRef, String> lineAppender) {
    return topicHelpHandlers();
  }
}
