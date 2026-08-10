package cafe.woden.ircclient.notify.api.store;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Feature-owned cooldown state for user notification rule-match events. */
public final class NotificationRuleMatchCooldown {
  static final Duration DEFAULT_KEY_TTL = Duration.ofHours(24);
  static final int DEFAULT_MAX_KEYS = 50_000;

  private final Duration keyTtl;
  private final int maxKeys;
  private final ConcurrentHashMap<Key, Instant> lastMatchAt = new ConcurrentHashMap<>();

  public NotificationRuleMatchCooldown() {
    this(DEFAULT_KEY_TTL, DEFAULT_MAX_KEYS);
  }

  NotificationRuleMatchCooldown(Duration keyTtl, int maxKeys) {
    this.keyTtl = keyTtl != null && !keyTtl.isNegative() ? keyTtl : DEFAULT_KEY_TTL;
    this.maxKeys = Math.max(1, maxKeys);
  }

  public static int normalizeCooldownSeconds(int configuredSeconds, int defaultSeconds) {
    return NotificationRuleCooldownPolicy.normalizeCooldownSeconds(
        configuredSeconds, defaultSeconds);
  }

  public boolean allow(
      String serverId, String channel, String ruleLabel, int cooldownSeconds, Instant now) {
    Key key = Key.of(serverId, channel, ruleLabel);
    if (key == null) return false;

    Instant safeNow = now != null ? now : Instant.now();
    prune(safeNow);

    long cooldownMs = (long) Math.max(0, cooldownSeconds) * 1_000L;
    boolean[] allowed = {false};

    lastMatchAt.compute(
        key,
        (k, previous) -> {
          if (previous != null
              && cooldownMs > 0L
              && safeNow.toEpochMilli() - previous.toEpochMilli() < cooldownMs) {
            return previous;
          }
          allowed[0] = true;
          return safeNow;
        });

    if (allowed[0]) {
      prune(safeNow);
    }

    return allowed[0];
  }

  public void clearChannel(String serverId, String channel) {
    String sid = keyPart(serverId);
    String chan = keyPart(channel);
    if (sid.isEmpty() || chan.isEmpty()) return;
    lastMatchAt
        .keySet()
        .removeIf(key -> key != null && sid.equals(key.serverId) && chan.equals(key.channel));
  }

  public void clearServer(String serverId) {
    String sid = keyPart(serverId);
    if (sid.isEmpty()) return;
    lastMatchAt.keySet().removeIf(key -> key != null && sid.equals(key.serverId));
  }

  public void clearRule(String serverId, String channel, String ruleLabel) {
    Key key = Key.of(serverId, channel, ruleLabel);
    if (key == null) return;
    lastMatchAt.remove(key);
  }

  public void clearSelectedRuleMatches(
      String serverId, Iterable<NotificationStoreEventValues> selectedRuleMatches) {
    String sid = keyPart(serverId);
    if (sid.isEmpty() || selectedRuleMatches == null) return;

    for (NotificationStoreEventValues event : selectedRuleMatches) {
      if (event == null || !event.valid()) continue;
      if (!sid.equals(keyPart(event.serverId()))) continue;
      clearRule(sid, event.channel(), event.label());
    }
  }

  int size() {
    return lastMatchAt.size();
  }

  private void prune(Instant now) {
    Instant cutoff = now.minus(keyTtl);
    lastMatchAt
        .entrySet()
        .removeIf(
            entry ->
                entry == null
                    || entry.getKey() == null
                    || entry.getValue() == null
                    || entry.getValue().isBefore(cutoff));

    int size = lastMatchAt.size();
    if (size <= maxKeys) return;

    int toRemove = size - maxKeys;
    for (int i = 0; i < toRemove; i++) {
      Key oldestKey = null;
      Instant oldestAt = null;
      for (Map.Entry<Key, Instant> entry : lastMatchAt.entrySet()) {
        if (entry == null || entry.getKey() == null) continue;
        Instant at = entry.getValue();
        if (at == null || oldestAt == null || at.isBefore(oldestAt)) {
          oldestAt = at;
          oldestKey = entry.getKey();
        }
      }
      if (oldestKey == null) break;
      lastMatchAt.remove(oldestKey);
    }
  }

  private static String keyPart(String value) {
    return Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
  }

  private static String ruleLabelKeyPart(String ruleLabel) {
    String label = Objects.toString(ruleLabel, "").trim();
    if (label.isEmpty()) label = "(rule)";
    return label.toLowerCase(Locale.ROOT);
  }

  private record Key(String serverId, String channel, String ruleLabel) {
    private static Key of(String serverId, String channel, String ruleLabel) {
      String sid = keyPart(serverId);
      String chan = keyPart(channel);
      if (sid.isEmpty() || chan.isEmpty()) return null;
      return new Key(sid, chan, ruleLabelKeyPart(ruleLabel));
    }
  }
}
