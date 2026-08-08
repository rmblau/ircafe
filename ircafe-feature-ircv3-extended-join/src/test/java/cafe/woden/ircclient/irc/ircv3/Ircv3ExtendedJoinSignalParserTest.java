package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3ExtendedJoinSignalParserTest {

  @Test
  void parsesAccountAndRealName() {
    Ircv3ExtendedJoinSignalParser.Observation observed =
        Ircv3ExtendedJoinSignalParser.parse(
                "alice", "JOIN", List.of("#ircafe", "acct", ":Alice Liddell"))
            .orElseThrow();

    assertEquals("#ircafe", observed.channel());
    assertEquals(Ircv3ExtendedJoinSignalParser.AccountState.LOGGED_IN, observed.accountState());
    assertEquals("acct", observed.accountName());
    assertEquals("Alice Liddell", observed.realName());
  }

  @Test
  void parsesLoggedOutAccountWithoutRealName() {
    Ircv3ExtendedJoinSignalParser.Observation observed =
        Ircv3ExtendedJoinSignalParser.parse("alice", "JOIN", List.of("#ircafe", "*"))
            .orElseThrow();

    assertEquals(Ircv3ExtendedJoinSignalParser.AccountState.LOGGED_OUT, observed.accountState());
    assertNull(observed.accountName());
    assertNull(observed.realName());
  }

  @Test
  void rejectsIncompleteJoinObservations() {
    assertTrue(Ircv3ExtendedJoinSignalParser.parse("alice", "JOIN", List.of("#ircafe")).isEmpty());
    assertTrue(Ircv3ExtendedJoinSignalParser.parse("alice", "JOIN", List.of("", "acct")).isEmpty());
  }
}
