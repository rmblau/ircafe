package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;
import java.util.Optional;

/** Validates and normalizes a persisted STS snapshot before it enters the active cache. */
public final class Ircv3StsPersistedPolicyNormalizer {

  public record Snapshot(
      long expiresAtEpochMs,
      Integer port,
      boolean preload,
      long durationSeconds,
      String rawValue) {
    public Snapshot {
      rawValue = Objects.toString(rawValue, "");
    }
  }

  public record Result(String hostLower, Optional<Ircv3StsPolicy> policy, boolean forgetPersisted) {
    public Result {
      hostLower = Objects.toString(hostLower, "").trim();
      policy = Objects.requireNonNullElse(policy, Optional.empty());
    }
  }

  public Result normalize(String host, Snapshot snapshot, long nowEpochMs) {
    String hostLower = Ircv3StsPolicy.normalizeHost(host);
    if (hostLower.isEmpty() || snapshot == null) {
      return new Result(hostLower, Optional.empty(), false);
    }
    if (snapshot.expiresAtEpochMs() <= nowEpochMs) {
      return new Result(hostLower, Optional.empty(), true);
    }

    Integer port = snapshot.port();
    if (port != null && (port <= 0 || port > 65_535)) {
      port = null;
    }
    long durationSeconds = snapshot.durationSeconds();
    if (durationSeconds <= 0L) {
      long remainingMs = Math.max(1L, snapshot.expiresAtEpochMs() - nowEpochMs);
      durationSeconds = Math.max(1L, remainingMs / 1000L);
    }

    Ircv3StsPolicy policy =
        new Ircv3StsPolicy(
            hostLower,
            snapshot.expiresAtEpochMs(),
            port,
            snapshot.preload(),
            durationSeconds,
            snapshot.rawValue());
    return new Result(hostLower, Optional.of(policy), false);
  }
}
