package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3MessageRedactionTagSignalTest {

  @Test
  void readsDeleteAndRedactAliases() {
    assertEquals(
        "message-1",
        Ircv3MessageRedactionTagSignal.fromTags(Map.of("+draft/delete", "message-1"))
            .orElseThrow()
            .messageId());
    assertEquals(
        "message 2",
        Ircv3MessageRedactionTagSignal.fromTags(Map.of("draft/redact", "message\\s2"))
            .orElseThrow()
            .messageId());
  }

  @Test
  void ignoresMissingRedactionTags() {
    assertTrue(Ircv3MessageRedactionTagSignal.fromTags(Map.of()).isEmpty());
  }
}
