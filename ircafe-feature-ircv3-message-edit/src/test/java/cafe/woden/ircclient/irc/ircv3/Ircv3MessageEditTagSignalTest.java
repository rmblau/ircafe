package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3MessageEditTagSignalTest {

  @Test
  void readsLegacyDraftEditTagWithClientPrefix() {
    assertEquals(
        "abc;123",
        Ircv3MessageEditTagSignal.fromTags(Map.of("+draft/edit", "abc\\:123"))
            .orElseThrow()
            .targetMessageId());
  }

  @Test
  void ignoresMissingEditTag() {
    assertTrue(Ircv3MessageEditTagSignal.fromTags(Map.of("msgid", "abc")).isEmpty());
  }
}
