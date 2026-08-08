package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3WhoisProbeTrackerTest {
  @Test
  void completesProbeWithObservedAwayAndAccountFacts() {
    Ircv3WhoisProbeTracker tracker = new Ircv3WhoisProbeTracker();
    tracker.begin(" Alice ");
    tracker.observeAway("alice");
    tracker.observeAccount("ALICE");

    Ircv3WhoisProbeTracker.Completion completion = tracker.complete("alice");

    assertTrue(completion.sawAway());
    assertTrue(completion.sawAccount());
    assertTrue(completion.accountNumericSupported());
    assertFalse(tracker.hasPending("alice"));
  }

  @Test
  void accountSupportIsLearnedEvenWithoutPendingProbe() {
    Ircv3WhoisProbeTracker tracker = new Ircv3WhoisProbeTracker();
    tracker.observeAccount("alice");
    tracker.begin("bob");

    Ircv3WhoisProbeTracker.Completion completion = tracker.complete("bob");

    assertFalse(completion.sawAway());
    assertFalse(completion.sawAccount());
    assertTrue(completion.accountNumericSupported());
  }

  @Test
  void duplicateBeginDoesNotEraseObservedFacts() {
    Ircv3WhoisProbeTracker tracker = new Ircv3WhoisProbeTracker();
    tracker.begin("alice");
    tracker.observeAway("alice");
    tracker.begin("ALICE");

    assertTrue(tracker.complete("alice").sawAway());
    assertNull(tracker.complete("alice"));
  }
}
