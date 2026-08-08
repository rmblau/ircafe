package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3AccountTagTrackerTest {

  @Test
  void emitsRawAccountChangesButSuppressesDuplicates() {
    Ircv3AccountTagTracker tracker = new Ircv3AccountTagTracker();

    Ircv3AccountTagTracker.Change login =
        tracker.observe("alice", Map.of("account", "alice")).orElseThrow();
    assertEquals(Ircv3AccountTagTracker.AccountState.LOGGED_IN, login.state());
    assertEquals("alice", login.accountName());
    assertTrue(tracker.observe("alice", Map.of("account", "alice")).isEmpty());

    Ircv3AccountTagTracker.Change star =
        tracker.observe("alice", Map.of("account", "*")).orElseThrow();
    assertEquals(Ircv3AccountTagTracker.AccountState.LOGGED_OUT, star.state());
    assertNull(star.accountName());

    Ircv3AccountTagTracker.Change zero =
        tracker.observe("alice", Map.of("account", "0")).orElseThrow();
    assertEquals(Ircv3AccountTagTracker.AccountState.LOGGED_OUT, zero.state());
    assertNull(zero.accountName());
  }

  @Test
  void acceptsRuntimeParsedRawAccountValues() {
    Ircv3AccountTagTracker tracker = new Ircv3AccountTagTracker();

    Ircv3AccountTagTracker.Change login =
        tracker.observe("alice", "alice-account").orElseThrow();
    Ircv3AccountTagTracker.Change logout = tracker.observe("alice", "*").orElseThrow();

    assertEquals(Ircv3AccountTagTracker.AccountState.LOGGED_IN, login.state());
    assertEquals("alice-account", login.accountName());
    assertEquals(Ircv3AccountTagTracker.AccountState.LOGGED_OUT, logout.state());
    assertNull(logout.accountName());
  }

  @Test
  void ignoresMissingAccountTagAndBoundsTrackedNicks() {
    Ircv3AccountTagTracker tracker = new Ircv3AccountTagTracker(2);
    assertTrue(tracker.observe("alice", Map.of("msgid", "1")).isEmpty());

    tracker.observe("alice", Map.of("account", "a"));
    tracker.observe("bob", Map.of("account", "b"));
    tracker.observe("carol", Map.of("account", "c"));
    assertEquals(2, tracker.trackedNickCount());
  }
}
