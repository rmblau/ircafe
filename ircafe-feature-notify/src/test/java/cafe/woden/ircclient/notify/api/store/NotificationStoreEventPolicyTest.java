package cafe.woden.ircclient.notify.api.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationStoreEventPolicyTest {

  @Test
  void normalizesHighlightEventValues() {
    NotificationStoreEventValues values =
        NotificationStoreEventPolicy.highlight(
            " libera ", " #ircafe ", " alice ", " ping ", " msg-1 ");

    assertTrue(values.valid());
    assertEquals("libera", values.serverId());
    assertEquals("#ircafe", values.channel());
    assertEquals("alice", values.fromNick());
    assertEquals("", values.label());
    assertEquals("ping", values.snippet());
    assertEquals("msg-1", values.messageId());
  }

  @Test
  void rejectsBlankServerOrChannelForChannelEvents() {
    assertFalse(
        NotificationStoreEventPolicy.highlight(" ", "#ircafe", "alice", "ping", "").valid());
    assertFalse(
        NotificationStoreEventPolicy.ruleMatch("libera", " ", "alice", "Rule", "ping", "").valid());
  }

  @Test
  void normalizesRuleMatchDefaults() {
    NotificationStoreEventValues values =
        NotificationStoreEventPolicy.ruleMatch("libera", "#ircafe", " ", " ", " ", null);

    assertTrue(values.valid());
    assertEquals(NotificationStoreEventPolicy.DEFAULT_NICK, values.fromNick());
    assertEquals(NotificationStoreEventPolicy.DEFAULT_RULE_LABEL, values.label());
    assertEquals("", values.snippet());
    assertEquals("", values.messageId());
  }

  @Test
  void ircEventDefaultsBlankTargetToStatus() {
    NotificationStoreEventValues values =
        NotificationStoreEventPolicy.ircEvent(
            " libera ", " ", " ", " Topic changed ", " body ", " msg-2 ");

    assertTrue(values.valid());
    assertEquals("libera", values.serverId());
    assertEquals(NotificationStoreEventPolicy.DEFAULT_IRC_EVENT_TARGET, values.channel());
    assertEquals(NotificationStoreEventPolicy.DEFAULT_NICK, values.fromNick());
    assertEquals("Topic changed", values.label());
    assertEquals("body", values.snippet());
    assertEquals("msg-2", values.messageId());
  }

  @Test
  void snippetIsTrimmedAndConservativelyTruncated() {
    String longSnippet = " " + "x".repeat(NotificationStoreEventPolicy.MAX_SNIPPET_CHARS + 5) + " ";

    String snippet = NotificationStoreEventPolicy.normalizeSnippet(longSnippet);

    assertEquals(NotificationStoreEventPolicy.MAX_SNIPPET_CHARS, snippet.length());
    assertTrue(snippet.endsWith("\u2026"));
  }
}
