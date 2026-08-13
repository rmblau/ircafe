package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3MultilineCommandPlannerTest {

  @Test
  void emptyPayloadPlansNoTransportLines() {
    Ircv3MultilineCommandPlanner.Plan plan =
        Ircv3MultilineCommandPlanner.plan(null, null, "", null, null, "libera");

    assertEquals(List.of(), plan.rawLines());
    assertFalse(plan.batched());
  }

  @Test
  void plansOneRawLineForSingleLinePayload() {
    Ircv3MultilineCommandPlanner.Plan plan =
        Ircv3MultilineCommandPlanner.plan(
            "privmsg",
            "#ircafe",
            "hello",
            new Ircv3MultilineCommandPlanner.NegotiatedState(false, false, 0L, 0L),
            "unused",
            "libera");

    assertEquals(List.of("PRIVMSG #ircafe :hello"), plan.rawLines());
    assertFalse(plan.batched());
  }

  @Test
  void plansFinalMultilineBatchWithConcatTags() {
    Ircv3MultilineCommandPlanner.Plan plan =
        Ircv3MultilineCommandPlanner.plan(
            "NOTICE",
            "#ircafe",
            "one\ntwo",
            new Ircv3MultilineCommandPlanner.NegotiatedState(true, true, 4096L, 4L),
            "mlabc",
            "libera");

    assertEquals(
        List.of(
            "BATCH +mlabc multiline #ircafe",
            "@batch=mlabc;+multiline-concat=1 NOTICE #ircafe :one",
            "@batch=mlabc NOTICE #ircafe :two",
            "BATCH -mlabc"),
        plan.rawLines());
    assertTrue(plan.batched());
  }

  @Test
  void plansDraftMultilineAndPreservesExistingFailureMessage() {
    Ircv3MultilineCommandPlanner.Plan plan =
        Ircv3MultilineCommandPlanner.plan(
            "PRIVMSG",
            "alice",
            "one\ntwo",
            new Ircv3MultilineCommandPlanner.NegotiatedState(false, true, 0L, 0L),
            "ml1",
            "libera");
    assertEquals("BATCH +ml1 draft/multiline alice", plan.rawLines().getFirst());

    IllegalArgumentException missing =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                Ircv3MultilineCommandPlanner.plan(
                    "PRIVMSG",
                    "#ircafe",
                    "one\ntwo",
                    new Ircv3MultilineCommandPlanner.NegotiatedState(false, false, 0L, 0L),
                    "ml1",
                    "libera"));
    assertEquals(
        "Message contains line breaks, but IRCv3 multiline is not negotiated: libera",
        missing.getMessage());
  }
}
