package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3HostmaskChangeTrackerTest {
  @Test
  void deduplicatesByCaseNormalizedNickname() {
    Ircv3HostmaskChangeTracker tracker = new Ircv3HostmaskChangeTracker();

    assertTrue(tracker.rememberIfChanged(" Alice ", "Alice!ident@host.example"));
    assertFalse(tracker.rememberIfChanged("alice", "Alice!ident@host.example"));
    assertTrue(tracker.rememberIfChanged("ALICE", "Alice!ident@new.example"));
  }

  @Test
  void rejectsBlankInputs() {
    Ircv3HostmaskChangeTracker tracker = new Ircv3HostmaskChangeTracker();

    assertFalse(tracker.rememberIfChanged("", "a!b@c"));
    assertFalse(tracker.rememberIfChanged("alice", " "));
  }
}
