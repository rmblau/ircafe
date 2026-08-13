package cafe.woden.ircclient.irc.ircv3;

import java.util.Locale;
import java.util.Objects;

/** Normalizes typing states and builds outbound IRCv3 typing TAGMSG commands. */
public final class Ircv3TypingCommandBuilder {

  private Ircv3TypingCommandBuilder() {}

  public static String normalizeState(String state) {
    String renderedState = Objects.toString(state, "").trim().toLowerCase(Locale.ROOT);
    if (renderedState.isEmpty()) return "";
    return switch (renderedState) {
      case "active", "composing" -> "active";
      case "paused" -> "paused";
      case "done", "inactive" -> "done";
      default -> "";
    };
  }

  public static String buildRawLine(String target, String state) {
    String outTarget = Ircv3CommandValuePolicy.normalizeTarget(target);
    String normalizedState = normalizeState(state);
    if (outTarget.isEmpty() || normalizedState.isEmpty()) return "";
    return "@+typing=" + normalizedState + " TAGMSG " + outTarget;
  }
}
