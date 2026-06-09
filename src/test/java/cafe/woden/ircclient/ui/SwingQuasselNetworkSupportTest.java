package cafe.woden.ircclient.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.irc.quassel.control.QuasselCoreControlPort;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SwingQuasselNetworkSupportTest {

  @Test
  void renderChoiceLabelUsesLocalizedFallbacksAndStatusText() {
    QuasselCoreControlPort.QuasselCoreNetworkSummary summary =
        new QuasselCoreControlPort.QuasselCoreNetworkSummary(
            42, "", false, false, 1, "irc.example.net", 6697, true, Map.of());

    assertEquals(
        "[42] network-42 - disconnected, disabled @ irc.example.net:6697 tls",
        SwingQuasselNetworkSupport.renderChoiceLabel(summary));
  }

  @Test
  void renderChoiceLabelUsesLocalizedNullSummaryFallback() {
    assertEquals("(unknown network)", SwingQuasselNetworkSupport.renderChoiceLabel(null));
  }

  @Test
  void renderChoiceLabelUsesLocalizedPlainTransport() {
    QuasselCoreControlPort.QuasselCoreNetworkSummary summary =
        new QuasselCoreControlPort.QuasselCoreNetworkSummary(
            7, "Libera", true, true, 1, "irc.libera.chat", 6667, false, Map.of());

    assertEquals(
        "[7] Libera - connected @ irc.libera.chat:6667 plain",
        SwingQuasselNetworkSupport.renderChoiceLabel(summary));
  }
}
