package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3StsTransportUpgradePlannerTest {

  private final Ircv3StsTransportUpgradePlanner planner = new Ircv3StsTransportUpgradePlanner();

  @Test
  void policyPortOverridesConfiguredTransportAndEnablesTls() {
    Ircv3StsPolicy policy =
        new Ircv3StsPolicy("irc.example.net", 10_000L, 6697, false, 60L, "duration=60");

    var plan = planner.plan(policy, 6667, false);

    assertEquals(6697, plan.port());
    assertTrue(plan.tls());
    assertTrue(plan.changed());
  }

  @Test
  void absentPolicyPortKeepsConfiguredPortAndAlreadySecureTransportIsUnchanged() {
    Ircv3StsPolicy policy =
        new Ircv3StsPolicy("irc.example.net", 10_000L, null, false, 60L, "duration=60");

    var plan = planner.plan(policy, 6697, true);

    assertEquals(6697, plan.port());
    assertTrue(plan.tls());
    assertFalse(plan.changed());
  }
}
