package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IrcEventNotificationCtcpTemplatePlannerTest {

  @Test
  void commandTemplatesSelectCtcpReceivedAndLikeCommandMatch() {
    IrcEventNotificationCtcpTemplatePlan plan =
        IrcEventNotificationCtcpTemplatePlanner.plan(" version ");

    assertEquals("CTCP_RECEIVED", plan.eventType());
    assertEquals("LIKE", plan.ctcpCommandMode());
    assertEquals("VERSION", plan.ctcpCommandPattern());
    assertEquals("ANY", plan.ctcpValueMode());
    assertEquals("", plan.ctcpValuePattern());
  }

  @Test
  void customOrUnknownTemplatesClearCtcpFilters() {
    IrcEventNotificationCtcpTemplatePlan custom =
        IrcEventNotificationCtcpTemplatePlanner.plan("CUSTOM");
    IrcEventNotificationCtcpTemplatePlan unknown =
        IrcEventNotificationCtcpTemplatePlanner.plan("ACTION");

    assertEquals("CTCP_RECEIVED", custom.eventType());
    assertEquals("ANY", custom.ctcpCommandMode());
    assertEquals("", custom.ctcpCommandPattern());
    assertEquals("ANY", custom.ctcpValueMode());
    assertEquals("", custom.ctcpValuePattern());
    assertEquals(custom, unknown);
  }
}
