package cafe.woden.ircclient.irc.ircv3;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Tracks bounded per-nick account-tag state and suppresses duplicate raw observations. */
public final class Ircv3AccountTagTracker {

  public static final int DEFAULT_MAX_TRACKED_NICKS = 8_192;

  public enum AccountState {
    LOGGED_IN,
    LOGGED_OUT
  }

  public record Change(String nick, AccountState state, String accountName) {
    public Change {
      nick = Objects.requireNonNull(nick, "nick");
      state = Objects.requireNonNull(state, "state");
      accountName = state == AccountState.LOGGED_OUT ? null : normalizeNullable(accountName);
    }
  }

  private final int maxTrackedNicks;
  private final LinkedHashMap<String, String> lastRawAccountByNickLower;

  public Ircv3AccountTagTracker() {
    this(DEFAULT_MAX_TRACKED_NICKS);
  }

  public Ircv3AccountTagTracker(int maxTrackedNicks) {
    if (maxTrackedNicks < 1) throw new IllegalArgumentException("maxTrackedNicks must be positive");
    this.maxTrackedNicks = maxTrackedNicks;
    this.lastRawAccountByNickLower = new LinkedHashMap<>(256, 0.75f, true);
  }

  public Optional<Change> observe(String nick, Map<String, String> tags) {
    if (tags == null || !tags.containsKey("account")) return Optional.empty();
    return observe(nick, tags.get("account"));
  }

  public synchronized Optional<Change> observe(String nick, String rawAccountValue) {
    if (rawAccountValue == null) return Optional.empty();

    String observedNick = Objects.requireNonNull(nick, "nick");
    String rawAccount = rawAccountValue.trim();
    String key = observedNick.toLowerCase(Locale.ROOT);
    String previous = lastRawAccountByNickLower.put(key, rawAccount);
    trimToLimit();
    if (Objects.equals(previous, rawAccount)) return Optional.empty();

    AccountState state =
        isLoggedOut(rawAccount) ? AccountState.LOGGED_OUT : AccountState.LOGGED_IN;
    return Optional.of(new Change(observedNick, state, rawAccount));
  }

  synchronized int trackedNickCount() {
    return lastRawAccountByNickLower.size();
  }

  private void trimToLimit() {
    while (lastRawAccountByNickLower.size() > maxTrackedNicks) {
      String eldest = lastRawAccountByNickLower.keySet().iterator().next();
      lastRawAccountByNickLower.remove(eldest);
    }
  }

  private static boolean isLoggedOut(String account) {
    return account.isEmpty() || "*".equals(account) || "0".equals(account);
  }

  private static String normalizeNullable(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
