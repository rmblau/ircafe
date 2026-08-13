package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class Ircv3EchoMessageTargetHintStoreTest {

  @Test
  void findsHintByMessageIdWithoutRequiringMatchingPayload() {
    Ircv3EchoMessageTargetHintStore store = new Ircv3EchoMessageTargetHintStore();
    long now = 1_000_000L;

    store.remember("WodenCafe", "alice", "PRIVMSG", "hello there", "msg-1", now);

    assertEquals(
        "alice", store.find("wodencafe", "PRIVMSG", "different text", "msg-1", now + 1_000));
  }

  @Test
  void fallsBackToCaseInsensitiveSenderAndKindFingerprint() {
    Ircv3EchoMessageTargetHintStore store = new Ircv3EchoMessageTargetHintStore();
    long now = 2_000_000L;

    store.remember("wodencafe", "bob", "ACTION", "waves", "", now);

    assertEquals("bob", store.find("WODENCAFE", "action", "waves", "", now + 500));
  }

  @Test
  void isolatesMessageIdMatchesBySenderAndKind() {
    Ircv3EchoMessageTargetHintStore store = new Ircv3EchoMessageTargetHintStore();
    long now = 3_000_000L;
    store.remember("me", "carol", "PRIVMSG", "hello", "shared-id", now);

    assertEquals("", store.find("someone-else", "PRIVMSG", "hello", "shared-id", now + 1));
    assertEquals("", store.find("me", "ACTION", "hello", "shared-id", now + 1));
  }

  @Test
  void expiresAndClearsHints() {
    Ircv3EchoMessageTargetHintStore store =
        new Ircv3EchoMessageTargetHintStore(Duration.ofSeconds(2), 10);
    long now = 4_000_000L;
    store.remember("me", "dave", "PRIVMSG", "old", "msg-old", now);

    assertEquals("", store.find("me", "PRIVMSG", "old", "msg-old", now + 2_001));

    store.remember("me", "erin", "PRIVMSG", "new", "msg-new", now + 3_000);
    store.clear();
    assertEquals("", store.find("me", "PRIVMSG", "new", "msg-new", now + 3_001));
  }

  @Test
  void hardCapClearsOnlyTheOverfullIndex() {
    Ircv3EchoMessageTargetHintStore store =
        new Ircv3EchoMessageTargetHintStore(Duration.ofMinutes(1), 1);
    long now = 5_000_000L;

    store.remember("me", "alice", "PRIVMSG", "one", "id-1", now);
    store.remember("me", "bob", "PRIVMSG", "two", "id-2", now + 1);
    store.remember("me", "carol", "PRIVMSG", "three", "id-3", now + 2);

    assertEquals("", store.find("me", "PRIVMSG", "three", "id-3", now + 3));
  }

  @Test
  void rejectsInvalidStoreConfiguration() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Ircv3EchoMessageTargetHintStore(Duration.ZERO, 1));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Ircv3EchoMessageTargetHintStore(Duration.ofSeconds(1), 0));
  }
}
