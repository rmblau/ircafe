package cafe.woden.ircclient.app.outbound.help.spi;

import cafe.woden.ircclient.model.TargetRef;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime /help contribution seam for outbound command services. */
@ApplicationLayer
public interface OutboundHelpContributor {

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
