package cafe.woden.ircclient.notify.api.sound;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/** Feature-owned timing policy for notification sound playback. */
public final class NotificationSoundPlaybackPolicy {
  private static final Duration MIN_INTERVAL = Duration.ofMillis(500);
  private static final long CLIP_FINISH_GRACE_MS = 1_500L;
  private static final long CLIP_WAIT_MIN_MS = 2_000L;
  private static final long CLIP_WAIT_MAX_MS = 30_000L;

  private NotificationSoundPlaybackPolicy() {}

  public static boolean canPlay(Instant lastPlayed, Instant now) {
    Instant safeLastPlayed = lastPlayed != null ? lastPlayed : Instant.EPOCH;
    Instant safeNow = now != null ? now : Instant.now();
    return Duration.between(safeLastPlayed, safeNow).compareTo(MIN_INTERVAL) > 0;
  }

  public static boolean isStalePreview(long previewSeq, long currentPreviewSeq) {
    return previewSeq > 0L && previewSeq != currentPreviewSeq;
  }

  public static boolean shouldStartPlayback(
      long previewSeq,
      long currentPreviewSeq,
      boolean bypassLimiter,
      Instant lastPlayed,
      Instant now) {
    if (isStalePreview(previewSeq, currentPreviewSeq)) return false;
    return bypassLimiter || canPlay(lastPlayed, now);
  }

  public static long clipWaitMillis(long clipMicrosecondLength) {
    long durationMs = TimeUnit.MICROSECONDS.toMillis(Math.max(0L, clipMicrosecondLength));
    return Math.max(
        CLIP_WAIT_MIN_MS, Math.min(CLIP_WAIT_MAX_MS, durationMs + CLIP_FINISH_GRACE_MS));
  }
}
