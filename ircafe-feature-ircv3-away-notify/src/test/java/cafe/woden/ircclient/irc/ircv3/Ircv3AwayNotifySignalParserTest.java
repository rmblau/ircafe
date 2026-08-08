package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3AwayNotifySignalParserTest {

  @Test
  void parsesAwayAndHostmask() {
    Ircv3AwayNotifySignalParser.Observation observed =
        Ircv3AwayNotifySignalParser.parse(
                "alice", "AWAY", ":alice!u@h AWAY :Gone", List.of(":Gone"))
            .orElseThrow();

    assertEquals("alice!u@h", observed.hostmask());
    assertTrue(observed.away());
    assertEquals("Gone", observed.message());
  }

  @Test
  void parsesReturnFromAway() {
    Ircv3AwayNotifySignalParser.Observation observed =
        Ircv3AwayNotifySignalParser.parse(
                "alice", "AWAY", ":alice!u@h AWAY", List.of())
            .orElseThrow();

    assertFalse(observed.away());
    assertNull(observed.message());
  }

  @Test
  void ignoresOtherCommandsAndBlankNicks() {
    assertTrue(
        Ircv3AwayNotifySignalParser.parse("alice", "ACCOUNT", "", List.of()).isEmpty());
    assertTrue(Ircv3AwayNotifySignalParser.parse("", "AWAY", "", List.of()).isEmpty());
  }
}
