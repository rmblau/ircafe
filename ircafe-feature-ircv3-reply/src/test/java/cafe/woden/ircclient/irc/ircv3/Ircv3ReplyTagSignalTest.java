package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3ReplyTagSignalTest {

  @Test
  void readsFinalAndLegacyReplyTags() {
    assertEquals(
        "final-1",
        Ircv3ReplyTagSignal.fromTags(Map.of("+reply", "final-1"))
            .orElseThrow()
            .replyToMessageId());
    assertEquals(
        "draft 1",
        Ircv3ReplyTagSignal.fromTags(Map.of("draft/reply", "draft\\s1"))
            .orElseThrow()
            .replyToMessageId());
  }

  @Test
  void ignoresMissingOrBlankReplyTags() {
    assertTrue(Ircv3ReplyTagSignal.fromTags(Map.of()).isEmpty());
    assertTrue(Ircv3ReplyTagSignal.fromTags(Map.of("reply", " ")).isEmpty());
  }
}
