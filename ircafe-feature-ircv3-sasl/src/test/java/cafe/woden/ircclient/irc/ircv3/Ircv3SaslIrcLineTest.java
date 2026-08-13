package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3SaslIrcLineTest {

  @Test
  void stripsTagsAndPrefixForNumericReplies() {
    Ircv3SaslIrcLine line =
        Ircv3SaslIrcLine.parse(
            "@time=2026-03-22T12:00:00Z :server.example 903 nick :SASL authentication successful");

    assertEquals("903", line.command());
    assertEquals("SASL authentication successful", line.trailing());
    assertTrue(line.isNumeric());
    assertEquals(903, line.numeric());
  }

  @Test
  void authenticateUsesNormalParameterInsteadOfTrailingField() {
    Ircv3SaslIrcLine line = Ircv3SaslIrcLine.parse(":server.example AUTHENTICATE +");

    assertEquals("AUTHENTICATE", line.command());
    assertEquals("+", line.trailing());
    assertFalse(line.isNumeric());
  }

  @Test
  void returnsNullForBlankPayloadAfterPrefixRemoval() {
    assertNull(Ircv3SaslIrcLine.parse(":server.example   "));
  }
}
