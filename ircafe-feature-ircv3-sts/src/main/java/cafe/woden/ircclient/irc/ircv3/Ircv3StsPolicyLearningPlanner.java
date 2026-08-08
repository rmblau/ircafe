package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;
import java.util.Optional;

/** Plans secure-only IRCv3 STS policy learning without owning cache or persistence effects. */
public final class Ircv3StsPolicyLearningPlanner {

  public enum Outcome {
    IGNORE_MISSING_HOST,
    IGNORE_EMPTY_VALUE,
    IGNORE_INSECURE_CONNECTION,
    IGNORE_INVALID_DIRECTIVE,
    CLEAR,
    LEARN
  }

  public record Decision(
      Outcome outcome, String hostLower, String rawValue, Optional<Ircv3StsPolicy> policy) {
    public Decision {
      outcome = Objects.requireNonNull(outcome, "outcome");
      hostLower = Objects.toString(hostLower, "").trim();
      rawValue = Objects.toString(rawValue, "").trim();
      policy = Objects.requireNonNullElse(policy, Optional.empty());
      if (outcome == Outcome.LEARN && policy.isEmpty()) {
        throw new IllegalArgumentException("LEARN decisions require a policy");
      }
      if (outcome != Outcome.LEARN && policy.isPresent()) {
        throw new IllegalArgumentException("only LEARN decisions may include a policy");
      }
    }

    public boolean changesStoredPolicy() {
      return outcome == Outcome.CLEAR || outcome == Outcome.LEARN;
    }
  }

  public Decision plan(String host, boolean secureConnection, String rawValue, long nowEpochMs) {
    String hostLower = Ircv3StsPolicy.normalizeHost(host);
    if (hostLower.isEmpty()) {
      return decision(Outcome.IGNORE_MISSING_HOST, hostLower, rawValue);
    }

    String value = Objects.toString(rawValue, "").trim();
    if (value.isEmpty()) {
      return decision(Outcome.IGNORE_EMPTY_VALUE, hostLower, value);
    }
    if (!secureConnection) {
      return decision(Outcome.IGNORE_INSECURE_CONNECTION, hostLower, value);
    }

    Ircv3StsPolicyDirective directive = Ircv3StsPolicyParser.parse(value).orElse(null);
    if (directive == null) {
      return decision(Outcome.IGNORE_INVALID_DIRECTIVE, hostLower, value);
    }
    if (directive.durationSeconds() == 0L) {
      return decision(Outcome.CLEAR, hostLower, value);
    }

    long expiresAt = addSaturated(nowEpochMs, toMillisSaturated(directive.durationSeconds()));
    Ircv3StsPolicy policy =
        new Ircv3StsPolicy(
            hostLower,
            expiresAt,
            directive.port(),
            directive.preload(),
            directive.durationSeconds(),
            value);
    return new Decision(Outcome.LEARN, hostLower, value, Optional.of(policy));
  }

  private static Decision decision(Outcome outcome, String hostLower, String rawValue) {
    return new Decision(outcome, hostLower, rawValue, Optional.empty());
  }

  static long toMillisSaturated(long seconds) {
    if (seconds <= 0L) return 0L;
    long max = Long.MAX_VALUE / 1000L;
    if (seconds >= max) return Long.MAX_VALUE;
    return seconds * 1000L;
  }

  static long addSaturated(long left, long right) {
    if (right <= 0L) return left;
    if (left >= Long.MAX_VALUE - right) return Long.MAX_VALUE;
    return left + right;
  }
}
