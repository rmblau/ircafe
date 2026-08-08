package cafe.woden.ircclient.irc.ircv3;

import java.util.Locale;
import java.util.Objects;

/** Immutable active IRCv3 STS policy independent of persistence and connection transport. */
public record Ircv3StsPolicy(
    String hostLower,
    long expiresAtEpochMs,
    Integer port,
    boolean preload,
    long durationSeconds,
    String rawValue) {

  public Ircv3StsPolicy {
    hostLower = normalizeHost(hostLower);
    if (hostLower.isEmpty()) {
      throw new IllegalArgumentException("hostLower must not be blank");
    }
    if (port != null && (port <= 0 || port > 65_535)) {
      throw new IllegalArgumentException("port must be between 1 and 65535");
    }
    if (durationSeconds <= 0L) {
      throw new IllegalArgumentException("durationSeconds must be positive");
    }
    rawValue = Objects.toString(rawValue, "").trim();
  }

  public boolean isExpired(long nowEpochMs) {
    return expiresAtEpochMs > 0L && nowEpochMs >= expiresAtEpochMs;
  }

  public static String normalizeHost(String host) {
    return Objects.toString(host, "").trim().toLowerCase(Locale.ROOT);
  }
}
