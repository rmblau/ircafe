package cafe.woden.ircclient.irc.ircv3;

import java.util.Locale;
import java.util.Objects;

/** Identifies self-authored bouncer bootstrap traffic that should not enter chat history. */
public final class Ircv3HistoryBootstrapSuppressionPolicy {

  private Ircv3HistoryBootstrapSuppressionPolicy() {}

  public static boolean shouldSuppress(boolean fromSelf, String target, String message) {
    if (!fromSelf) {
      return false;
    }
    if (isZncPlayStarCursorCommand(message)) {
      return true;
    }

    String normalizedTarget = Objects.toString(target, "").trim();
    if (normalizedTarget.isEmpty()) {
      return false;
    }
    if ("*playback".equalsIgnoreCase(normalizedTarget)
        && message != null
        && message.toLowerCase(Locale.ROOT).startsWith("play ")) {
      return true;
    }
    return "*status".equalsIgnoreCase(normalizedTarget) && "ListNetworks".equalsIgnoreCase(message);
  }

  public static boolean isZncPlayStarCursorCommand(String message) {
    String normalized = Objects.toString(message, "").trim();
    if (normalized.isEmpty()) {
      return false;
    }
    String[] parts = normalized.split("\\s+");
    if (parts.length < 3 || !"play".equalsIgnoreCase(parts[0]) || !"*".equals(parts[1])) {
      return false;
    }
    String count = parts[2];
    if (count.isEmpty()) {
      return false;
    }
    for (int i = 0; i < count.length(); i++) {
      if (!Character.isDigit(count.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
