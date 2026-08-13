package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class Ircv3ReadMarkerTimestampTest {

  private static final Instant FALLBACK = Instant.parse("2026-03-22T12:30:00Z");

  @Test
  void parsesTimestampWrapperIsoSecondsAndMilliseconds() {
    assertEquals(
        Instant.parse("2026-03-22T12:05:00Z").toEpochMilli(),
        Ircv3ReadMarkerTimestamp.parseEpochMs("timestamp=2026-03-22T12:05:00Z", FALLBACK));
    assertEquals(1_711_111_111_000L, Ircv3ReadMarkerTimestamp.parseEpochMs("1711111111", FALLBACK));
    assertEquals(
        1_711_111_111_222L, Ircv3ReadMarkerTimestamp.parseEpochMs("1711111111222", FALLBACK));
  }

  @Test
  void usesZeroForWildcardAndFallbackForMalformedOrNonPositiveValues() {
    assertEquals(0L, Ircv3ReadMarkerTimestamp.parseEpochMs("*", FALLBACK));
    assertEquals(
        FALLBACK.toEpochMilli(), Ircv3ReadMarkerTimestamp.parseEpochMs("invalid", FALLBACK));
    assertEquals(FALLBACK.toEpochMilli(), Ircv3ReadMarkerTimestamp.parseEpochMs("0", FALLBACK));
  }
}
