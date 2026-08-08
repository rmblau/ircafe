package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3LabeledResponseTagSignalTest {

  @Test
  void extractsLabelFromTagsAndRawLines() {
    assertEquals(
        "req-1",
        Ircv3LabeledResponseTagSignal.fromTags(Map.of("label", "req-1")).orElseThrow());
    assertEquals(
        "req;2",
        Ircv3LabeledResponseTagSignal.fromRawLine("@label=req\\:2 :server 200 me :ok")
            .orElseThrow());
    assertTrue(Ircv3LabeledResponseTagSignal.fromTags(Map.of()).isEmpty());
  }

  @Test
  void classifiesFailAsFailureAndOtherStandardRepliesAsSuccess() {
    assertEquals(
        Ircv3LabeledResponseTagSignal.Outcome.FAILURE,
        Ircv3LabeledResponseTagSignal.outcomeForStandardReply("FAIL"));
    assertEquals(
        Ircv3LabeledResponseTagSignal.Outcome.SUCCESS,
        Ircv3LabeledResponseTagSignal.outcomeForStandardReply("WARN"));
  }
}
