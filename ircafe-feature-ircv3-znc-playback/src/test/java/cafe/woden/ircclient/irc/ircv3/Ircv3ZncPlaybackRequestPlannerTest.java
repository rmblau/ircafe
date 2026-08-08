package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class Ircv3ZncPlaybackRequestPlannerTest {

  private final Ircv3ZncPlaybackRequestPlanner planner = new Ircv3ZncPlaybackRequestPlanner();

  @Test
  void plansExplicitPlaybackRange() {
    Ircv3ZncPlaybackRequestPlanner.Plan plan =
        planner.plan(
            " #ircafe ",
            Instant.ofEpochSecond(10),
            Instant.ofEpochSecond(20),
            Instant.ofEpochSecond(99));

    assertEquals("#ircafe", plan.target());
    assertEquals(Instant.ofEpochSecond(10), plan.fromInclusive());
    assertEquals(Instant.ofEpochSecond(20), plan.toInclusive());
    assertEquals("play #ircafe 10 20", plan.renderCommand("#ircafe"));
  }

  @Test
  void omittedUpperBoundUsesNowForCaptureButNotCommand() {
    Ircv3ZncPlaybackRequestPlanner.Plan plan =
        planner.plan("alice", null, null, Instant.ofEpochSecond(42));

    assertEquals(Instant.EPOCH, plan.fromInclusive());
    assertEquals(Instant.ofEpochSecond(42), plan.toInclusive());
    assertEquals("play alice 0", plan.renderCommand("alice"));
  }

  @Test
  void nonPositiveUpperBoundMatchesLegacyOpenEndedCommand() {
    Ircv3ZncPlaybackRequestPlanner.Plan plan =
        planner.plan("#ircafe", Instant.ofEpochSecond(10), Instant.EPOCH, Instant.ofEpochSecond(99));

    assertEquals(Instant.EPOCH, plan.toInclusive());
    assertEquals("play #ircafe 10", plan.renderCommand("#ircafe"));
  }

  @Test
  void rejectsBlankTargets() {
    assertThrows(IllegalArgumentException.class, () -> planner.plan(" ", null, null));
  }
}
