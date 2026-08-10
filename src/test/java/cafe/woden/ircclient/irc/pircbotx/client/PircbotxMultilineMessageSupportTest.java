package cafe.woden.ircclient.irc.pircbotx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.irc.ircv3.Ircv3OutboundCommandRuntimeCatalog;
import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pircbotx.PircBotX;
import org.pircbotx.output.OutputRaw;

class PircbotxMultilineMessageSupportTest {

  @Test
  void singleLinePrivmsgUsesSingleRawLine() {
    PircbotxMultilineMessageSupport support =
        new PircbotxMultilineMessageSupport(
            Ircv3OutboundCommandRuntimeCatalog.applicationClasspath());
    PircbotxConnectionState connection = new PircbotxConnectionState("libera");
    PircBotX bot = mock(PircBotX.class);
    OutputRaw outputRaw = mock(OutputRaw.class);
    when(bot.sendRaw()).thenReturn(outputRaw);

    support.send(bot, connection, "libera", "#ircafe", "hello", false);

    verify(outputRaw).rawLine("PRIVMSG #ircafe :hello");
  }

  @Test
  void multilineMessageRequiresNegotiatedCapability() {
    PircbotxMultilineMessageSupport support =
        new PircbotxMultilineMessageSupport(
            Ircv3OutboundCommandRuntimeCatalog.applicationClasspath());
    PircbotxConnectionState connection = new PircbotxConnectionState("libera");
    PircBotX bot = mock(PircBotX.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> support.send(bot, connection, "libera", "#ircafe", "hello\nworld", false));
  }

  @Test
  void negotiatedMultilineSendsFeaturePlannedBatchLines() {
    PircbotxMultilineMessageSupport support =
        new PircbotxMultilineMessageSupport(
            Ircv3OutboundCommandRuntimeCatalog.applicationClasspath());
    PircbotxConnectionState connection = new PircbotxConnectionState("libera");
    connection.setDraftMultilineCapAcked(true);
    PircBotX bot = mock(PircBotX.class);
    OutputRaw outputRaw = mock(OutputRaw.class);
    when(bot.sendRaw()).thenReturn(outputRaw);

    support.send(bot, connection, "libera", "#ircafe", "one\ntwo", false);

    ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
    verify(outputRaw, times(4)).rawLine(lines.capture());
    List<String> sent = lines.getAllValues();
    assertTrue(sent.get(0).matches("BATCH \\+ml[0-9a-z]+ draft/multiline #ircafe"));
    String batchId = sent.get(0).split(" ")[1].substring(1);
    assertEquals(
        "@batch=" + batchId + ";+draft/multiline-concat=1 PRIVMSG #ircafe :one", sent.get(1));
    assertEquals("@batch=" + batchId + " PRIVMSG #ircafe :two", sent.get(2));
    assertEquals("BATCH -" + batchId, sent.get(3));
  }
}
