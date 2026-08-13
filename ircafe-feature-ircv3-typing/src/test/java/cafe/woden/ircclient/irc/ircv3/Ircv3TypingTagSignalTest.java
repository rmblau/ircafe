package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3TypingTagSignalTest {

  @Test
  void readsClientOnlyTypingState() {
    assertEquals(
        "active", Ircv3TypingTagSignal.fromTags(Map.of("+typing", "active")).orElseThrow().state());
  }

  @Test
  void ignoresMissingOrBlankTypingState() {
    assertTrue(Ircv3TypingTagSignal.fromTags(Map.of()).isEmpty());
    assertTrue(Ircv3TypingTagSignal.fromTags(Map.of("typing", " ")).isEmpty());
  }
}
