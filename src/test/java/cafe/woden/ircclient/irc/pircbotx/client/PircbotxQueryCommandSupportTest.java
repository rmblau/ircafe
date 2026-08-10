package cafe.woden.ircclient.irc.pircbotx.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import org.junit.jupiter.api.Test;
import org.pircbotx.PircBotX;
import org.pircbotx.output.OutputRaw;

class PircbotxQueryCommandSupportTest {

  private final PircbotxQueryCommandSupport support = new PircbotxQueryCommandSupport();

  @Test
  void requestNamesSendsSanitizedNamesCommand() {
    PircBotX bot = mock(PircBotX.class);
    OutputRaw outputRaw = mock(OutputRaw.class);
    when(bot.sendRaw()).thenReturn(outputRaw);

    support.requestNames(bot, "  #ircafe ");

    verify(outputRaw).rawLine("NAMES #ircafe");
  }

  @Test
  void whoisTracksNickStateAndSendsWhoisCommand() {
    PircBotX bot = mock(PircBotX.class);
    OutputRaw outputRaw = mock(OutputRaw.class);
    when(bot.sendRaw()).thenReturn(outputRaw);
    PircbotxConnectionState connection = new PircbotxConnectionState("libera");

    support.whois(connection, bot, " Alice ");

    verify(outputRaw).rawLine("WHOIS Alice");
    assertTrue(connection.hasPendingWhoisProbe("alice"));
    var completion = connection.completeWhoisProbe("alice");
    assertFalse(completion.sawAway());
    assertFalse(completion.sawAccount());
  }

  @Test
  void whowasOmitsCountWhenNonPositive() {
    PircBotX bot = mock(PircBotX.class);
    OutputRaw outputRaw = mock(OutputRaw.class);
    when(bot.sendRaw()).thenReturn(outputRaw);

    support.whowas(bot, "Alice", 0);

    verify(outputRaw).rawLine("WHOWAS Alice");
  }

  @Test
  void whowasIncludesCountWhenPositive() {
    PircBotX bot = mock(PircBotX.class);
    OutputRaw outputRaw = mock(OutputRaw.class);
    when(bot.sendRaw()).thenReturn(outputRaw);

    support.whowas(bot, "Alice", 5);

    verify(outputRaw).rawLine("WHOWAS Alice 5");
  }
}
