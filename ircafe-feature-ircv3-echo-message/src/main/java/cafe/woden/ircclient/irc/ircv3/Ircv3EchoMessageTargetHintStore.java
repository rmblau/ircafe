package cafe.woden.ircclient.irc.ircv3;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bounded, expiring correlation state for mapping self-authored echo-message traffic back to its
 * private-conversation target.
 */
public final class Ircv3EchoMessageTargetHintStore {

  public static final Duration DEFAULT_TTL = Duration.ofMinutes(2);
  public static final int DEFAULT_MAX_ENTRIES = 1_024;

  private record TargetHint(
      String fromLower, String target, String kind, String payload, long observedAtMs) {}

  private final long ttlMs;
  private final int maxEntries;
  private final Map<String, TargetHint> byMessageId = new ConcurrentHashMap<>();
  private final Map<String, TargetHint> byFingerprint = new ConcurrentHashMap<>();

  public Ircv3EchoMessageTargetHintStore() {
    this(DEFAULT_TTL, DEFAULT_MAX_ENTRIES);
  }

  public Ircv3EchoMessageTargetHintStore(Duration ttl, int maxEntries) {
    Duration normalizedTtl = Objects.requireNonNullElse(ttl, DEFAULT_TTL);
    if (normalizedTtl.isZero() || normalizedTtl.isNegative()) {
      throw new IllegalArgumentException("ttl must be positive");
    }
    if (maxEntries <= 0) {
      throw new IllegalArgumentException("maxEntries must be positive");
    }
    this.ttlMs = normalizedTtl.toMillis();
    this.maxEntries = maxEntries;
  }

  public void remember(
      String fromNick,
      String target,
      String kind,
      String payload,
      String messageId,
      long observedAtMs) {
    String from = normalizeLower(fromNick);
    String destination = normalizeTarget(target);
    String normalizedKind = normalizeKind(kind);
    String body = normalizePayload(payload);
    String msgId = normalizeMessageId(messageId);
    if (from.isEmpty() || destination.isEmpty() || normalizedKind.isEmpty()) {
      return;
    }
    if (body.isEmpty() && msgId.isEmpty()) {
      return;
    }

    long now = positiveOrCurrentTime(observedAtMs);
    cleanup(now);

    TargetHint hint = new TargetHint(from, destination, normalizedKind, body, now);
    if (!msgId.isEmpty()) {
      byMessageId.put(msgId, hint);
    }
    if (!body.isEmpty()) {
      byFingerprint.put(fingerprint(from, normalizedKind, body), hint);
    }
  }

  public String find(
      String fromNick, String kind, String payload, String messageId, long observedAtMs) {
    String from = normalizeLower(fromNick);
    String normalizedKind = normalizeKind(kind);
    String body = normalizePayload(payload);
    String msgId = normalizeMessageId(messageId);
    long now = positiveOrCurrentTime(observedAtMs);
    if (from.isEmpty() || normalizedKind.isEmpty()) {
      return "";
    }

    cleanup(now);

    if (!msgId.isEmpty()) {
      TargetHint byId = byMessageId.get(msgId);
      if (isUsableById(byId, from, normalizedKind, now)) {
        return byId.target();
      }
    }

    if (!body.isEmpty()) {
      TargetHint byPayload = byFingerprint.get(fingerprint(from, normalizedKind, body));
      if (isUsableByFingerprint(byPayload, from, normalizedKind, body, now)) {
        return byPayload.target();
      }
    }
    return "";
  }

  public void clear() {
    byMessageId.clear();
    byFingerprint.clear();
  }

  private boolean isUsableById(TargetHint hint, String from, String kind, long now) {
    if (hint == null || isExpired(hint, now)) {
      return false;
    }
    return Objects.equals(hint.fromLower(), from) && Objects.equals(hint.kind(), kind);
  }

  private boolean isUsableByFingerprint(
      TargetHint hint, String from, String kind, String payload, long now) {
    return isUsableById(hint, from, kind, now) && Objects.equals(hint.payload(), payload);
  }

  private boolean isExpired(TargetHint hint, long now) {
    return hint.observedAtMs() < saturatedSubtract(now, ttlMs);
  }

  private void cleanup(long now) {
    long cutoff = saturatedSubtract(now, ttlMs);
    byMessageId
        .entrySet()
        .removeIf(entry -> entry.getValue() == null || entry.getValue().observedAtMs() < cutoff);
    byFingerprint
        .entrySet()
        .removeIf(entry -> entry.getValue() == null || entry.getValue().observedAtMs() < cutoff);
    enforceHardCap();
  }

  private void enforceHardCap() {
    int hardCap = saturatedDouble(maxEntries);
    if (byMessageId.size() > hardCap) {
      byMessageId.clear();
    }
    if (byFingerprint.size() > hardCap) {
      byFingerprint.clear();
    }
  }

  private static int saturatedDouble(int value) {
    return value > Integer.MAX_VALUE / 2 ? Integer.MAX_VALUE : value * 2;
  }

  private static long saturatedSubtract(long value, long decrement) {
    try {
      return Math.subtractExact(value, decrement);
    } catch (ArithmeticException ignored) {
      return Long.MIN_VALUE;
    }
  }

  private static long positiveOrCurrentTime(long value) {
    return value > 0L ? value : System.currentTimeMillis();
  }

  private static String normalizeLower(String raw) {
    String value = Objects.toString(raw, "").trim();
    return value.isEmpty() ? "" : value.toLowerCase(java.util.Locale.ROOT);
  }

  private static String normalizeTarget(String raw) {
    return Objects.toString(raw, "").trim();
  }

  private static String normalizeKind(String raw) {
    String value = Objects.toString(raw, "").trim().toUpperCase(java.util.Locale.ROOT);
    return switch (value) {
      case "PRIVMSG", "ACTION" -> value;
      default -> "";
    };
  }

  private static String normalizePayload(String raw) {
    return Objects.toString(raw, "").trim();
  }

  private static String normalizeMessageId(String raw) {
    return Objects.toString(raw, "").trim();
  }

  private static String fingerprint(String fromLower, String kind, String payload) {
    return fromLower + '\n' + kind + '\n' + payload;
  }
}
