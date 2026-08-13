package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3IsupportLineTest {

  @Test
  void parsesTagsPrefixValuesAndRemovedTokens() {
    Ircv3IsupportLine line =
        Ircv3IsupportLine.parse(
                "@time=2026-07-12T00:00:00Z :server 005 me MONITOR=100 -WHOX CLIENTTAGDENY=*,-typing :supported")
            .orElseThrow();

    assertEquals(3, line.tokens().size());
    assertEquals("100", line.lastToken("monitor").orElseThrow().value());
    assertTrue(line.lastToken("WHOX").orElseThrow().removed());
    assertEquals("*,-typing", line.lastToken("clienttagdeny").orElseThrow().value());
  }

  @Test
  void lastOccurrenceControlsEnabledState() {
    Ircv3IsupportLine line =
        Ircv3IsupportLine.parse(":server 005 me WHOX -WHOX MONITOR MONITOR=42 :supported")
            .orElseThrow();

    assertFalse(line.hasEnabledToken("whox"));
    assertTrue(line.hasEnabledToken("monitor"));
    assertEquals("42", line.lastToken("monitor").orElseThrow().value());
  }

  @Test
  void rejectsNonIsupportLines() {
    assertTrue(Ircv3IsupportLine.parse(":server 004 me server version modes").isEmpty());
    assertTrue(Ircv3IsupportLine.parse("").isEmpty());
  }
}
