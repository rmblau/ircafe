package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificationPanelEventRowPlannerTest {
  @Test
  void highlightUsesLocalizedMentionLabelAndSafeSnippet() {
    NotificationPanelEventRowPlan plan =
        NotificationPanelEventRowPlanner.highlight("Mention", null);

    assertEquals("Mention", plan.match());
    assertEquals("", plan.snippet());
  }

  @Test
  void ruleMatchUsesRuleLabelAndSnippet() {
    NotificationPanelEventRowPlan plan =
        NotificationPanelEventRowPlanner.ruleMatch("Security", "matched phrase");

    assertEquals("Security", plan.match());
    assertEquals("matched phrase", plan.snippet());
  }

  @Test
  void ircEventUsesTitleAndBody() {
    NotificationPanelEventRowPlan plan =
        NotificationPanelEventRowPlanner.ircEvent("Invite received", "alice invited you");

    assertEquals("Invite received", plan.match());
    assertEquals("alice invited you", plan.snippet());
  }

  @Test
  void newestFirstSortsLaterInstantsBeforeOlderInstants() {
    Instant earlier = Instant.parse("2026-07-07T10:00:00Z");
    Instant later = Instant.parse("2026-07-07T11:00:00Z");

    assertTrue(NotificationPanelEventRowPlanner.compareNewestFirst(later, earlier) < 0);
    assertTrue(NotificationPanelEventRowPlanner.compareNewestFirst(earlier, later) > 0);
    assertEquals(0, NotificationPanelEventRowPlanner.compareNewestFirst(earlier, earlier));
  }

  @Test
  void newestFirstPlacesNullInstantsLast() {
    Instant at = Instant.parse("2026-07-07T10:00:00Z");

    assertTrue(NotificationPanelEventRowPlanner.compareNewestFirst(null, at) > 0);
    assertTrue(NotificationPanelEventRowPlanner.compareNewestFirst(at, null) < 0);
    assertEquals(0, NotificationPanelEventRowPlanner.compareNewestFirst(null, null));
  }
}
