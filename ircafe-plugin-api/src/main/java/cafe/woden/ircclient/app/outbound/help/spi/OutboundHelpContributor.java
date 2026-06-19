package cafe.woden.ircclient.app.outbound.help.spi;

import java.util.Map;
import java.util.function.Consumer;

/**
 * ServiceLoader-backed runtime /help contribution seam for outbound command services.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.outbound.help.spi.OutboundHelpContributor}.
 */
public interface OutboundHelpContributor {

  default void appendGeneralHelp(OutboundHelpSink help) {}

  default Map<String, Consumer<OutboundHelpSink>> topicHelpHandlers() {
    return Map.of();
  }
}
