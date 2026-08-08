package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class Ircv3ServerTimeTest {

  @Test
  void parsesServerTimeFromRawLinesAndEvents() {
    Instant expected = Instant.parse("2026-07-11T12:34:56.789Z");

    assertEquals(
        expected,
        Ircv3ServerTime.fromRawLine("@time=2026-07-11T12:34:56.789Z :server NOTICE nick :hi")
            .orElseThrow());
    assertEquals(expected, Ircv3ServerTime.fromEvent(new TimedEvent()).orElseThrow());
    assertEquals(
        expected,
        Ircv3ServerTime.fromTags(java.util.Map.of("time", expected.toString())).orElseThrow());
    assertEquals(
        expected,
        Ircv3ServerTime.fromTagsOrRawLine(
                java.util.Map.of(), "@time=" + expected + " :server NOTICE nick :hi")
            .orElseThrow());
  }

  @Test
  void ignoresInvalidOrMissingTimesAndUsesFallback() {
    assertTrue(
        Ircv3ServerTime.fromRawLine("@time=not-an-instant :server NOTICE nick :hi")
            .isEmpty());
    assertNull(Ircv3ServerTime.parseServerTimeFromRawLine("NOTICE nick :hi"));

    Instant fallback = Instant.parse("2026-07-11T13:00:00Z");
    assertSame(fallback, Ircv3ServerTime.orNow(Optional.empty(), fallback));
  }

  private static final class TimedEvent {
    public Map<String, String> getTags() {
      return Map.of("time", "2026-07-11T12:34:56.789Z");
    }
  }
}
