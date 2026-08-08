package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class Ircv3SaslFailureSignalTest {

  @Test
  void parsesKnownFailureNumericAndTrailingMessage() {
    Ircv3SaslFailureSignal signal =
        Ircv3SaslFailureSignal.parse(":server 905 me :SASL message too long");

    assertEquals(905, signal.numeric());
    assertEquals("SASL message too long", signal.trailingMessage());
    assertEquals(
        "Login failed — SASL authentication failed (payload too long): SASL message too long",
        signal.disconnectReason());
  }

  @Test
  void avoidsRepeatingCanonicalFailureMessage() {
    Ircv3SaslFailureSignal signal =
        Ircv3SaslFailureSignal.parse(":server 904 me :SASL authentication failed");

    assertEquals("Login failed — SASL authentication failed", signal.disconnectReason());
  }

  @Test
  void ignoresNonFailureNumerics() {
    assertNull(Ircv3SaslFailureSignal.parse(":server 900 me account :You are logged in"));
  }
}
