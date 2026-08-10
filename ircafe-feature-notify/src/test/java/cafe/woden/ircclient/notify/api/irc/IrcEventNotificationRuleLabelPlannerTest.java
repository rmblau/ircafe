package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IrcEventNotificationRuleLabelPlannerTest {

  @Test
  void combinesEventAndSourceLabelsWhenBothArePresent() {
    IrcEventNotificationRuleLabelPlan plan =
        IrcEventNotificationRuleLabelPlanner.plan(" Invite Received ", " Someone else ", "Event");

    assertEquals("Invite Received", plan.eventLabel());
    assertEquals("Someone else", plan.sourceLabel());
    assertEquals("Invite Received (Someone else)", plan.displayLabel());
  }

  @Test
  void omitsBlankSourceLabels() {
    IrcEventNotificationRuleLabelPlan plan =
        IrcEventNotificationRuleLabelPlanner.plan("Kicked", "  ", "Event");

    assertEquals("Kicked", plan.displayLabel());
  }

  @Test
  void usesFallbackWhenEventLabelIsBlank() {
    IrcEventNotificationRuleLabelPlan plan =
        IrcEventNotificationRuleLabelPlanner.plan("  ", "Any", "Event");

    assertEquals("Event", plan.eventLabel());
    assertEquals("Event (Any)", plan.displayLabel());
  }
}
