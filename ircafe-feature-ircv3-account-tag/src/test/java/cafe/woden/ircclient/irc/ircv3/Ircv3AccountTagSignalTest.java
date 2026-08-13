package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3AccountTagSignalTest {

  @Test
  void parsesPresentAccountTagWithoutCollapsingLogoutRepresentations() {
    Ircv3AccountTagSignal loggedIn =
        Ircv3AccountTagSignal.fromTags(" alice ", Map.of("account", " alice-account "))
            .orElseThrow();
    Ircv3AccountTagSignal loggedOut =
        Ircv3AccountTagSignal.fromTags("alice", Map.of("account", "*")).orElseThrow();

    assertEquals("alice", loggedIn.nick());
    assertEquals("alice-account", loggedIn.rawAccount());
    assertEquals("*", loggedOut.rawAccount());
  }

  @Test
  void ignoresMissingTagOrBlankNick() {
    assertTrue(Ircv3AccountTagSignal.fromTags("alice", Map.of("msgid", "1")).isEmpty());
    assertTrue(Ircv3AccountTagSignal.fromTags(" ", Map.of("account", "alice")).isEmpty());
  }
}
