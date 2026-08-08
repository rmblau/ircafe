package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationDispatchContextPlannerTest {

  @Test
  void rejectsBlankServerId() {
    IrcEventNotificationDispatchContext context =
        IrcEventNotificationDispatchContextPlanner.plan(
            "KLINED", " ", "#ircafe", "alice", "Title", "Body", "libera", "#ircafe");

    assertFalse(context.valid());
  }

  @Test
  void normalizesDispatchDefaultsAndText() {
    IrcEventNotificationDispatchContext context =
        IrcEventNotificationDispatchContextPlanner.plan(
            " KLINED ", " libera ", " ", " ", " ", " body ", " ", " #active ");

    assertTrue(context.valid());
    assertEquals("libera", context.serverId());
    assertEquals("status", context.target());
    assertEquals("server", context.sourceNick());
    assertEquals("KLINED", context.title());
    assertEquals("body", context.body());
    assertEquals("#active", context.activeTarget());
    assertFalse(context.activeTargetOnSameServer());
  }

  @Test
  void detectsActiveTargetOnSameServerCaseInsensitively() {
    IrcEventNotificationDispatchContext context =
        IrcEventNotificationDispatchContextPlanner.plan(
            "TOPIC_CHANGED",
            "Libera",
            "#ircafe",
            "alice",
            "Topic changed",
            "changed",
            "libera",
            "#ircafe");

    assertTrue(context.valid());
    assertTrue(context.activeTargetOnSameServer());
  }
}
