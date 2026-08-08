package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class Ircv3ServerTimeLagSampleTest {

  @Test
  void derivesLagFromObservedAndTaggedTimes() {
    Ircv3ServerTimeLagSample sample =
        Ircv3ServerTimeLagSample.from(Instant.ofEpochMilli(9_250L), 10_000L).orElseThrow();

    assertEquals(750L, sample.lagMs());
    assertEquals(10_000L, sample.observedAtMs());
  }

  @Test
  void futureServerTimestampClampsToZero() {
    Ircv3ServerTimeLagSample sample =
        Ircv3ServerTimeLagSample.from(Instant.ofEpochMilli(11_000L), 10_000L).orElseThrow();

    assertEquals(0L, sample.lagMs());
  }

  @Test
  void rejectsSamplesOutsidePassiveLagWindow() {
    assertTrue(
        Ircv3ServerTimeLagSample.from(Instant.EPOCH, 600_001L).isEmpty(),
        "samples older than five minutes should not replace a recent probe");
  }
}
