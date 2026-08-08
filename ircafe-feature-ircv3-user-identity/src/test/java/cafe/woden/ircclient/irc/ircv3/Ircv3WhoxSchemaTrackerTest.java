package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3WhoxSchemaTrackerTest {
  @Test
  void compatibleAndIncompatibleSignalsAreEachEmittedOnce() {
    Ircv3WhoxSchemaTracker tracker = new Ircv3WhoxSchemaTracker();

    assertTrue(tracker.observeCompatible());
    assertFalse(tracker.observeCompatible());
    assertTrue(tracker.compatible());

    assertTrue(tracker.observeIncompatible());
    assertFalse(tracker.observeIncompatible());
    assertFalse(tracker.compatible());
  }
}
