package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3StsPolicyLearningPlannerTest {

  private final Ircv3StsPolicyLearningPlanner planner = new Ircv3StsPolicyLearningPlanner();

  @Test
  void learnsSecurePolicyWithNormalizedHostAndExpiry() {
    long now = 1_000L;
    var decision =
        planner.plan(" IRC.Example.NET ", true, "duration=60,port=6697,preload", now);

    assertEquals(Ircv3StsPolicyLearningPlanner.Outcome.LEARN, decision.outcome());
    Ircv3StsPolicy policy = decision.policy().orElseThrow();
    assertEquals("irc.example.net", policy.hostLower());
    assertEquals(61_000L, policy.expiresAtEpochMs());
    assertEquals(6697, policy.port());
    assertTrue(policy.preload());
    assertTrue(decision.changesStoredPolicy());
  }

  @Test
  void durationZeroPlansClear() {
    var decision = planner.plan("irc.example.net", true, "duration=0", 1_000L);

    assertEquals(Ircv3StsPolicyLearningPlanner.Outcome.CLEAR, decision.outcome());
    assertTrue(decision.policy().isEmpty());
    assertTrue(decision.changesStoredPolicy());
  }

  @Test
  void insecureAndInvalidDirectivesAreIgnored() {
    assertEquals(
        Ircv3StsPolicyLearningPlanner.Outcome.IGNORE_INSECURE_CONNECTION,
        planner.plan("irc.example.net", false, "duration=60", 1_000L).outcome());
    assertEquals(
        Ircv3StsPolicyLearningPlanner.Outcome.IGNORE_INVALID_DIRECTIVE,
        planner.plan("irc.example.net", true, "duration=nope", 1_000L).outcome());
    assertFalse(
        planner
            .plan("irc.example.net", false, "duration=60", 1_000L)
            .changesStoredPolicy());
  }

  @Test
  void expiryArithmeticSaturates() {
    var decision =
        planner.plan("irc.example.net", true, "duration=9223372036854775807", Long.MAX_VALUE - 5L);

    assertEquals(Long.MAX_VALUE, decision.policy().orElseThrow().expiresAtEpochMs());
  }
}
