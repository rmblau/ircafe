package cafe.woden.ircclient.irc.ircv3;

import java.time.Instant;
import java.util.Objects;

/** Validates and plans ZNC playback range requests independently of an IRC transport. */
public final class Ircv3ZncPlaybackRequestPlanner {

  public Plan plan(String target, Instant fromInclusive, Instant toInclusive) {
    return plan(target, fromInclusive, toInclusive, Instant.now());
  }

  Plan plan(String target, Instant fromInclusive, Instant toInclusive, Instant now) {
    String normalizedTarget = Objects.toString(target, "").trim();
    if (normalizedTarget.isEmpty()) {
      throw new IllegalArgumentException("target is blank");
    }

    Instant from = fromInclusive == null ? Instant.EPOCH : fromInclusive;
    Instant captureTo = toInclusive == null ? Objects.requireNonNull(now, "now") : toInclusive;
    return new Plan(
        normalizedTarget,
        from,
        captureTo,
        toInclusive != null && toInclusive.getEpochSecond() > 0L);
  }

  public record Plan(
      String target, Instant fromInclusive, Instant toInclusive, boolean includeUpperBound) {
    public Plan {
      target = Objects.requireNonNull(target, "target");
      fromInclusive = Objects.requireNonNull(fromInclusive, "fromInclusive");
      toInclusive = Objects.requireNonNull(toInclusive, "toInclusive");
    }

    public String renderCommand(String renderedTarget) {
      String targetValue = Objects.toString(renderedTarget, "").trim();
      if (targetValue.isEmpty()) {
        throw new IllegalArgumentException("rendered target is blank");
      }
      String command = "play " + targetValue + " " + fromInclusive.getEpochSecond();
      if (includeUpperBound) {
        command += " " + toInclusive.getEpochSecond();
      }
      return command;
    }
  }
}
