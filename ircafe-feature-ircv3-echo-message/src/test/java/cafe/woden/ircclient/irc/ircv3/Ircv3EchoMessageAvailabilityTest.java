package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3EchoMessageAvailabilityTest {

  @Test
  void requiresBothLiveSessionAndNegotiatedCapability() {
    assertFalse(Ircv3EchoMessageAvailability.isAvailable(false, false));
    assertFalse(Ircv3EchoMessageAvailability.isAvailable(true, false));
    assertFalse(Ircv3EchoMessageAvailability.isAvailable(false, true));
    assertTrue(Ircv3EchoMessageAvailability.isAvailable(true, true));
  }
}
