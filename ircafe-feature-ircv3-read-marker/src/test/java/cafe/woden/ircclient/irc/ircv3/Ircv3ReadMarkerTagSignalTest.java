package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3ReadMarkerTagSignalTest {

  @Test
  void readsFinalAndDraftMarkerTagsWithEscapeDecoding() {
    assertEquals(
        "timestamp=2026-03-22T12;05;00Z",
        Ircv3ReadMarkerTagSignal.fromTags(
                Map.of("+draft/read-marker", "timestamp=2026-03-22T12\\:05\\:00Z"))
            .orElseThrow()
            .marker());
    assertEquals(
        "*", Ircv3ReadMarkerTagSignal.fromTags(Map.of("read-marker", "*")).orElseThrow().marker());
  }

  @Test
  void ignoresMissingOrBlankMarkerTags() {
    assertTrue(Ircv3ReadMarkerTagSignal.fromTags(Map.of()).isEmpty());
    assertTrue(Ircv3ReadMarkerTagSignal.fromTags(Map.of("read-marker", " ")).isEmpty());
  }
}
