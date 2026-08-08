package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class Ircv3ReadMarkerCommandBuilderTest {

  @Test
  void buildsMillisecondPrecisionMarkreadLine() {
    assertEquals(
        "MARKREAD #ircafe timestamp=2026-03-23T12:05:00.000Z",
        Ircv3ReadMarkerCommandBuilder.buildTimestampRawLine(
            "#ircafe", Instant.parse("2026-03-23T12:05:00Z")));
  }
}
