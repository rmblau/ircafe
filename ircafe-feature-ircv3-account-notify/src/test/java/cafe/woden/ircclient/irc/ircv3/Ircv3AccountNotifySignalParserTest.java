package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3AccountNotifySignalParserTest {

  @Test
  void parsesLoggedOutAndHostmask() {
    Ircv3AccountNotifySignalParser.Observation observed =
        Ircv3AccountNotifySignalParser.parse(
                "alice", "ACCOUNT", ":alice!u@h ACCOUNT *", List.of("*"))
            .orElseThrow();

    assertEquals(Ircv3AccountNotifySignalParser.AccountState.LOGGED_OUT, observed.state());
    assertNull(observed.accountName());
    assertEquals("alice!u@h", observed.hostmask());
  }

  @Test
  void parsesLoggedInAccountName() {
    Ircv3AccountNotifySignalParser.Observation observed =
        Ircv3AccountNotifySignalParser.parse(
                "alice", "ACCOUNT", ":alice!u@h ACCOUNT alice-account", List.of("alice-account"))
            .orElseThrow();

    assertEquals(Ircv3AccountNotifySignalParser.AccountState.LOGGED_IN, observed.state());
    assertEquals("alice-account", observed.accountName());
  }

  @Test
  void ignoresOtherCommandsAndBlankNicks() {
    assertTrue(Ircv3AccountNotifySignalParser.parse("alice", "AWAY", "", List.of()).isEmpty());
    assertTrue(Ircv3AccountNotifySignalParser.parse("", "ACCOUNT", "", List.of()).isEmpty());
  }
}
