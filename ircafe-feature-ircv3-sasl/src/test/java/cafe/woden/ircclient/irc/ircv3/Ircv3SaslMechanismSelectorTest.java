package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3SaslMechanismSelectorTest {

  private final Ircv3SaslMechanismSelector selector = new Ircv3SaslMechanismSelector();

  @Test
  void explicitMechanismIsHonored() {
    assertEquals("PLAIN", selector.choose("plain", "user", "secret", Set.of("SCRAM-SHA-256")));
  }

  @Test
  void autoPrefersStrongestPasswordMechanismOffered() {
    assertEquals(
        "SCRAM-SHA-256",
        selector.choose("AUTO", "user", "secret", Set.of("PLAIN", "SCRAM-SHA-1", "SCRAM-SHA-256")));
  }

  @Test
  void autoFallsBackToExternalWithoutSecret() {
    assertEquals("EXTERNAL", selector.choose("AUTO", "", "", Set.of("EXTERNAL", "PLAIN")));
  }

  @Test
  void autoReturnsNullWhenPasswordMechanismNeedsUsername() {
    assertNull(selector.choose("AUTO", "", "secret", Set.of("PLAIN", "SCRAM-SHA-256")));
  }

  @Test
  void autoMakesConservativeGuessWhenServerDidNotListMechanisms() {
    assertEquals("PLAIN", selector.choose("AUTO", "user", "secret", Set.of()));
  }
}
