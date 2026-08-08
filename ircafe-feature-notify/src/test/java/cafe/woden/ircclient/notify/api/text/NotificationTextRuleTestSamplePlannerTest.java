package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationTextRuleTestSamplePlannerTest {

  @Test
  void normalizesNullSampleToEmptyMatcherText() {
    NotificationTextRuleTestSamplePlan plan = NotificationTextRuleTestSamplePlanner.plan(null, 800);

    assertEquals("", plan.rawSample());
    assertEquals("", plan.matcherSample());
    assertTrue(plan.empty());
    assertFalse(plan.truncated());
    assertEquals(0, plan.originalLength());
  }

  @Test
  void trimsMatcherSampleButKeepsBoundedRawSampleForDisplayParity() {
    NotificationTextRuleTestSamplePlan plan =
        NotificationTextRuleTestSamplePlanner.plan("  alice says ping  ", 800);

    assertEquals("  alice says ping  ", plan.rawSample());
    assertEquals("alice says ping", plan.matcherSample());
    assertFalse(plan.empty());
    assertFalse(plan.truncated());
  }

  @Test
  void truncatesLongSamplesBeforeMatcherTrimming() {
    NotificationTextRuleTestSamplePlan plan =
        NotificationTextRuleTestSamplePlanner.plan("  0123456789  ", 6);

    assertEquals("  0123", plan.rawSample());
    assertEquals("0123", plan.matcherSample());
    assertFalse(plan.empty());
    assertTrue(plan.truncated());
    assertEquals(14, plan.originalLength());
  }

  @Test
  void treatsWhitespaceOnlyBoundedSamplesAsEmpty() {
    NotificationTextRuleTestSamplePlan plan = NotificationTextRuleTestSamplePlanner.plan("   ", 2);

    assertEquals("  ", plan.rawSample());
    assertEquals("", plan.matcherSample());
    assertTrue(plan.empty());
    assertTrue(plan.truncated());
    assertEquals(3, plan.originalLength());
  }

  @Test
  void negativeMaximumBehavesLikeZeroCharactersAllowed() {
    NotificationTextRuleTestSamplePlan plan = NotificationTextRuleTestSamplePlanner.plan("ping", -1);

    assertEquals("", plan.rawSample());
    assertEquals("", plan.matcherSample());
    assertTrue(plan.empty());
    assertTrue(plan.truncated());
    assertEquals(4, plan.originalLength());
  }
}
