package cafe.woden.ircclient.irc.ircv3;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Thread-safe lifecycle state for IRCafe-initiated WHOIS probes. */
public final class Ircv3WhoisProbeTracker {
  private final ConcurrentHashMap<String, ProbeState> pendingByNickLower =
      new ConcurrentHashMap<>();
  private final AtomicBoolean accountNumericSupported = new AtomicBoolean(false);

  public record Completion(
      String nick, boolean sawAway, boolean sawAccount, boolean accountNumericSupported) {}

  private record ProbeState(boolean sawAway, boolean sawAccount) {}

  public void begin(String nick) {
    String key = normalizedNick(nick);
    if (key != null) {
      pendingByNickLower.putIfAbsent(key, new ProbeState(false, false));
    }
  }

  public void observeAway(String nick) {
    String key = normalizedNick(nick);
    if (key != null) {
      pendingByNickLower.computeIfPresent(
          key, (ignored, state) -> new ProbeState(true, state.sawAccount()));
    }
  }

  public void observeAccount(String nick) {
    accountNumericSupported.set(true);
    String key = normalizedNick(nick);
    if (key != null) {
      pendingByNickLower.computeIfPresent(
          key, (ignored, state) -> new ProbeState(state.sawAway(), true));
    }
  }

  public Completion complete(String nick) {
    String key = normalizedNick(nick);
    if (key == null) return null;
    ProbeState state = pendingByNickLower.remove(key);
    if (state == null) return null;
    return new Completion(
        nick.trim(), state.sawAway(), state.sawAccount(), accountNumericSupported.get());
  }

  public boolean hasPending(String nick) {
    String key = normalizedNick(nick);
    return key != null && pendingByNickLower.containsKey(key);
  }

  public boolean accountNumericSupported() {
    return accountNumericSupported.get();
  }

  private static String normalizedNick(String nick) {
    if (nick == null || nick.isBlank()) return null;
    return nick.trim().toLowerCase(Locale.ROOT);
  }
}
