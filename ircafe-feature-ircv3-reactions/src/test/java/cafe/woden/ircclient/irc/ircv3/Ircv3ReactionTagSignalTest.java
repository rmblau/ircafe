package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3ReactionTagSignalTest {

  @Test
  void emitsReactThenUnreactAndPrefersReplyTargetMetadata() {
    List<Ircv3ReactionTagSignal> signals =
        Ircv3ReactionTagSignal.fromTags(
            Map.of(
                "draft/react", ":+1:",
                "draft/unreact", ":-1:",
                "draft/reply", "reply-1",
                "msgid", "message-1"));

    assertEquals(2, signals.size());
    assertEquals(Ircv3ReactionTagSignal.Operation.REACT, signals.get(0).operation());
    assertEquals(":+1:", signals.get(0).reaction());
    assertEquals("reply-1", signals.get(0).messageId());
    assertEquals(Ircv3ReactionTagSignal.Operation.UNREACT, signals.get(1).operation());
  }

  @Test
  void fallsBackToMessageIdAndIgnoresMissingReactionTags() {
    Ircv3ReactionTagSignal signal =
        Ircv3ReactionTagSignal.fromTags(Map.of("draft/react", "wave", "draft/msgid", "m-2"))
            .getFirst();

    assertEquals("m-2", signal.messageId());
    assertTrue(Ircv3ReactionTagSignal.fromTags(Map.of("msgid", "m-3")).isEmpty());
  }
}
