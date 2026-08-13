package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3AwayLineParserTest {

  @Test
  void parsesAwayNotifySetAndClear() {
    Ircv3AwayLineParser.AwayNotify away =
        Ircv3AwayLineParser.parseAwayNotify(":alice!u@h AWAY :Gone away");
    assertNotNull(away);
    assertEquals("alice", away.nick());
    assertTrue(away.away());
    assertEquals("Gone away", away.message());

    Ircv3AwayLineParser.AwayNotify here = Ircv3AwayLineParser.parseAwayNotify(":alice!u@h AWAY");
    assertNotNull(here);
    assertFalse(here.away());
    assertNull(here.message());
  }

  @Test
  void parsesSelfAwayConfirmation() {
    Ircv3AwayLineParser.AwayConfirmation confirmation =
        Ircv3AwayLineParser.parseRpl305or306Away(
            ":server 306 me :You have been marked as being away");

    assertNotNull(confirmation);
    assertTrue(confirmation.away());
    assertEquals("server", confirmation.server());
    assertEquals("You have been marked as being away", confirmation.message());
  }
}
