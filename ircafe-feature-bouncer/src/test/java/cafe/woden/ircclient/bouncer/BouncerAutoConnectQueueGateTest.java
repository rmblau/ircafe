package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BouncerAutoConnectQueueGateTest {

  private final BouncerAutoConnectQueueGate gate = new BouncerAutoConnectQueueGate();

  @Test
  void markQueuedAcceptsEachServerIdOnce() {
    assertTrue(gate.markQueued(" bouncer:origin-one:libera "));
    assertFalse(gate.markQueued("bouncer:origin-one:libera"));
    assertTrue(gate.isQueued("bouncer:origin-one:libera"));
  }

  @Test
  void markQueuedRejectsBlankServerIds() {
    assertFalse(gate.markQueued(" "));
    assertFalse(gate.markQueued(null));
  }

  @Test
  void clearOriginRemovesOnlyMatchingGeneratedServerIds() {
    assertTrue(gate.markQueued("bouncer:origin-one:libera"));
    assertTrue(gate.markQueued("bouncer:origin-two:oftc"));
    assertTrue(gate.markQueued("plain-server"));

    gate.clearOrigin("origin-one");

    assertFalse(gate.isQueued("bouncer:origin-one:libera"));
    assertTrue(gate.isQueued("bouncer:origin-two:oftc"));
    assertTrue(gate.isQueued("plain-server"));
  }

  @Test
  void clearOriginIgnoresBlankOrigins() {
    assertTrue(gate.markQueued("bouncer:origin-one:libera"));

    gate.clearOrigin(" ");

    assertTrue(gate.isQueued("bouncer:origin-one:libera"));
  }
}
