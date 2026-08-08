package cafe.woden.ircclient.notify.api.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificationSoundPlaybackPolicyTest {

  @Test
  void requiresMoreThanMinimumIntervalBetweenRegularPlaybackAttempts() {
    Instant last = Instant.parse("2026-07-04T12:00:00Z");

    assertFalse(NotificationSoundPlaybackPolicy.canPlay(last, last.plusMillis(500)));
    assertTrue(NotificationSoundPlaybackPolicy.canPlay(last, last.plusMillis(501)));
  }

  @Test
  void treatsMissingLastPlayedAsPlayable() {
    assertTrue(NotificationSoundPlaybackPolicy.canPlay(null, Instant.parse("2026-07-04T12:00:00Z")));
  }

  @Test
  void detectsStalePreviewRequests() {
    assertFalse(NotificationSoundPlaybackPolicy.isStalePreview(0L, 3L));
    assertFalse(NotificationSoundPlaybackPolicy.isStalePreview(3L, 3L));
    assertTrue(NotificationSoundPlaybackPolicy.isStalePreview(2L, 3L));
  }

  @Test
  void startPlaybackCombinesStalePreviewAndRateLimitDecisions() {
    Instant last = Instant.parse("2026-07-04T12:00:00Z");
    Instant now = last.plusMillis(100);

    assertFalse(NotificationSoundPlaybackPolicy.shouldStartPlayback(2L, 3L, true, last, now));
    assertTrue(NotificationSoundPlaybackPolicy.shouldStartPlayback(3L, 3L, true, last, now));
    assertFalse(NotificationSoundPlaybackPolicy.shouldStartPlayback(0L, 0L, false, last, now));
    assertTrue(
        NotificationSoundPlaybackPolicy.shouldStartPlayback(
            0L, 0L, false, last, last.plusMillis(501)));
  }

  @Test
  void clampsClipWaitTime() {
    assertEquals(2_000L, NotificationSoundPlaybackPolicy.clipWaitMillis(0L));
    assertEquals(
        11_500L,
        NotificationSoundPlaybackPolicy.clipWaitMillis(Duration.ofSeconds(10).toNanos() / 1_000L));
    assertEquals(
        30_000L,
        NotificationSoundPlaybackPolicy.clipWaitMillis(Duration.ofMinutes(2).toNanos() / 1_000L));
  }
}
