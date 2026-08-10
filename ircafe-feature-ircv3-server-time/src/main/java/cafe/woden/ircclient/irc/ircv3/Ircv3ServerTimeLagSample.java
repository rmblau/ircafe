package cafe.woden.ircclient.irc.ircv3;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** Derives a bounded passive lag sample from an IRCv3 server-time timestamp. */
public record Ircv3ServerTimeLagSample(long lagMs, long observedAtMs) {

  public static final Duration MAX_PASSIVE_LAG = Duration.ofMinutes(5);

  public Ircv3ServerTimeLagSample {
    if (lagMs < 0L) {
      throw new IllegalArgumentException("lagMs must not be negative");
    }
    if (observedAtMs <= 0L) {
      throw new IllegalArgumentException("observedAtMs must be positive");
    }
  }

  public static Optional<Ircv3ServerTimeLagSample> from(Instant serverTaggedAt, long observedAtMs) {
    if (serverTaggedAt == null) {
      return Optional.empty();
    }
    long observed = observedAtMs > 0L ? observedAtMs : System.currentTimeMillis();
    long taggedAtMs;
    try {
      taggedAtMs = serverTaggedAt.toEpochMilli();
    } catch (ArithmeticException ignored) {
      return Optional.empty();
    }
    long lag = saturatedDifference(observed, taggedAtMs);
    if (lag < 0L) {
      lag = 0L;
    }
    if (lag > MAX_PASSIVE_LAG.toMillis()) {
      return Optional.empty();
    }
    return Optional.of(new Ircv3ServerTimeLagSample(lag, observed));
  }

  private static long saturatedDifference(long left, long right) {
    try {
      return Math.subtractExact(left, right);
    } catch (ArithmeticException ignored) {
      return left >= right ? Long.MAX_VALUE : Long.MIN_VALUE;
    }
  }
}
