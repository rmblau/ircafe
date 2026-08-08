package cafe.woden.ircclient.notify.api.store;

/** Feature-owned range and default policy for notification rule-match cooldowns. */
public final class NotificationRuleCooldownPolicy {
  public static final int DEFAULT_COOLDOWN_SECONDS = 15;
  public static final int MIN_COOLDOWN_SECONDS = 0;
  public static final int MAX_COOLDOWN_SECONDS = 3_600;

  private NotificationRuleCooldownPolicy() {}

  public static int normalizeCooldownSeconds(int configuredSeconds) {
    return normalizeCooldownSeconds(configuredSeconds, DEFAULT_COOLDOWN_SECONDS);
  }

  public static int normalizeCooldownSeconds(int configuredSeconds, int defaultSeconds) {
    int fallback = clamp(defaultSeconds);
    if (configuredSeconds < MIN_COOLDOWN_SECONDS) {
      return fallback;
    }
    return clamp(configuredSeconds);
  }

  private static int clamp(int seconds) {
    return Math.max(MIN_COOLDOWN_SECONDS, Math.min(seconds, MAX_COOLDOWN_SECONDS));
  }
}
