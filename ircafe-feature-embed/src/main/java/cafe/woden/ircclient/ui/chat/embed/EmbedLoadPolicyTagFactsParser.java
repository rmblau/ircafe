package cafe.woden.ircclient.ui.chat.embed;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Feature-owned interpretation of IRCv3 tags used by embed load policy sender facts. */
@Component
@InterfaceLayer
@Lazy
public class EmbedLoadPolicyTagFactsParser {

  private final Clock clock;

  public EmbedLoadPolicyTagFactsParser() {
    this(Clock.systemUTC());
  }

  EmbedLoadPolicyTagFactsParser(Clock clock) {
    this.clock = clock != null ? clock : Clock.systemUTC();
  }

  public EmbedLoadPolicyTagFacts parse(Map<String, String> tags) {
    if (tags == null || tags.isEmpty()) {
      return EmbedLoadPolicyTagFacts.empty();
    }

    boolean loggedInKnown = false;
    boolean loggedIn = false;
    String accountTag = firstTagValue(tags, "account");
    if (!accountTag.isBlank()) {
      loggedInKnown = true;
      loggedIn = !("*".equals(accountTag) || "0".equals(accountTag));
    }

    return EmbedLoadPolicyTagFacts.of(loggedInKnown, loggedIn, parseAccountAgeDays(tags));
  }

  private long parseAccountAgeDays(Map<String, String> tags) {
    long days = parsePositiveLong(firstAnyTagValue(tags, "account-age-days", "account_age_days"));
    if (days >= 0) return days;

    long seconds =
        parsePositiveLong(
            firstAnyTagValue(tags, "account-age-seconds", "account_age_seconds", "account-age"));
    if (seconds >= 0) {
      return seconds / 86_400L;
    }

    String createdRaw =
        firstAnyTagValue(
            tags,
            "account-created",
            "account_created",
            "account-ts",
            "account_ts",
            "account-registered",
            "account_registered");
    Instant createdAt = parseInstantLike(createdRaw);
    if (createdAt == null) return EmbedLoadPolicySenderFacts.UNKNOWN_ACCOUNT_AGE_DAYS;
    long age = ChronoUnit.DAYS.between(createdAt, Instant.now(clock));
    return age < 0 ? EmbedLoadPolicySenderFacts.UNKNOWN_ACCOUNT_AGE_DAYS : age;
  }

  private static String firstAnyTagValue(Map<String, String> tags, String... keys) {
    if (tags == null || tags.isEmpty() || keys == null) return "";
    for (String key : keys) {
      String value = firstTagValue(tags, key);
      if (!value.isBlank()) return value;
    }
    return "";
  }

  private static String firstTagValue(Map<String, String> tags, String key) {
    if (tags == null || tags.isEmpty()) return "";
    String wanted = normalizeTagKey(key);
    if (wanted.isEmpty()) return "";
    for (Map.Entry<String, String> entry : tags.entrySet()) {
      String got = normalizeTagKey(entry.getKey());
      if (!wanted.equals(got)) continue;
      return Objects.toString(entry.getValue(), "").trim();
    }
    return "";
  }

  private static long parsePositiveLong(String raw) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty()) return -1L;
    try {
      long parsed = Long.parseLong(value);
      return parsed < 0 ? -1L : parsed;
    } catch (Exception ignored) {
      return -1L;
    }
  }

  private static Instant parseInstantLike(String raw) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty()) return null;

    Long numeric = null;
    try {
      numeric = Long.parseLong(value);
    } catch (Exception ignored) {
      numeric = null;
    }
    if (numeric != null) {
      if (numeric > 100_000_000_000L) {
        return Instant.ofEpochMilli(numeric);
      }
      return Instant.ofEpochSecond(numeric);
    }

    try {
      return Instant.parse(value);
    } catch (Exception ignored) {
      return null;
    }
  }

  private static String normalizeTagKey(String raw) {
    String k = Objects.toString(raw, "").trim();
    if (k.startsWith("@")) k = k.substring(1).trim();
    if (k.startsWith("+")) k = k.substring(1).trim();
    if (k.isEmpty()) return "";
    return k.toLowerCase(Locale.ROOT);
  }
}
