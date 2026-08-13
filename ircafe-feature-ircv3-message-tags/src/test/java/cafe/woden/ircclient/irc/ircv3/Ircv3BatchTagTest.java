package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3BatchTagTest {

  @Test
  void extractsTrimmedBatchIdsFromRawLinesAndEvents() {
    assertEquals(
        "history-42",
        Ircv3BatchTag.fromRawLine("@batch=history-42 :server PRIVMSG #c :hi").orElseThrow());
    assertEquals("event-7", Ircv3BatchTag.fromEvent(new TaggedEvent()).orElseThrow());
    assertEquals("map-9", Ircv3BatchTag.fromTags(Map.of("@Batch", " map-9 ")).orElseThrow());
  }

  @Test
  void rejectsBlankOrMissingBatchIds() {
    assertTrue(Ircv3BatchTag.fromRawLine("@batch= :server PRIVMSG #c :hi").isEmpty());
    assertTrue(
        Ircv3BatchTag.fromRawLine("@time=2026-07-11T12:00:00Z :server PRIVMSG #c :hi").isEmpty());
    assertTrue(Ircv3BatchTag.fromEvent(null).isEmpty());
    assertTrue(Ircv3BatchTag.fromTags(Map.of("batch", " ")).isEmpty());
  }

  private static final class TaggedEvent {
    public Map<String, String> getTags() {
      return Map.of("@Batch", " event-7 ");
    }
  }
}
