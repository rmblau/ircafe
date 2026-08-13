package cafe.woden.ircclient.irc.ircv3;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Per-connection hostmask deduplication keyed by case-normalized nickname. */
public final class Ircv3HostmaskChangeTracker {
  private final ConcurrentHashMap<String, String> lastHostmaskByNickLower =
      new ConcurrentHashMap<>();

  public boolean rememberIfChanged(String nick, String hostmask) {
    String key = normalizedNick(nick);
    if (key == null || hostmask == null || hostmask.isBlank()) return false;
    String previous = lastHostmaskByNickLower.put(key, hostmask);
    return !Objects.equals(previous, hostmask);
  }

  private static String normalizedNick(String nick) {
    if (nick == null || nick.isBlank()) return null;
    return nick.trim().toLowerCase(Locale.ROOT);
  }
}
