package cafe.woden.ircclient.irc.ircv3;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Pure CLIENTTAGDENY parsing and client-only message-tag policy. */
public final class Ircv3ClientTagPolicy {

  private Ircv3ClientTagPolicy() {}

  /**
   * Parses one RPL_ISUPPORT (005) line and returns the raw CLIENTTAGDENY value.
   *
   * <p>Returns {@code null} when the token is absent. Returns an empty string when the token is
   * present without a value or as {@code CLIENTTAGDENY=}.
   */
  public static String parseRpl005ClientTagDenyValue(String line) {
    return Ircv3IsupportLine.parse(line)
        .flatMap(parsed -> parsed.lastToken("CLIENTTAGDENY"))
        .map(token -> token.removed() ? "" : token.value())
        .orElse(null);
  }

  /**
   * Returns whether a client-only tag is allowed by a CLIENTTAGDENY value.
   *
   * <p>The tag name must not include the client-only {@code +} prefix. A blank deny value allows
   * all client-only tags. A leading {@code *} blocks all tags except following {@code -tag}
   * exceptions.
   */
  public static boolean isClientOnlyTagAllowed(String clientTagDenyValue, String rawTagName) {
    String deny = Objects.toString(clientTagDenyValue, "").trim();
    if (deny.isEmpty()) return true;

    String tag = normalizeTagName(rawTagName);
    if (tag.isEmpty()) return true;

    String[] items = deny.split(",");
    boolean catchAllBlocked = false;
    Set<String> blocked = new HashSet<>();
    Set<String> exceptions = new HashSet<>();

    for (int i = 0; i < items.length; i++) {
      String raw = Objects.toString(items[i], "").trim();
      if (raw.isEmpty()) continue;
      if (i == 0 && "*".equals(raw)) {
        catchAllBlocked = true;
        continue;
      }
      if (raw.startsWith("-") && raw.length() > 1) {
        String exception = normalizeTagName(raw.substring(1));
        if (!exception.isEmpty()) exceptions.add(exception);
      } else {
        String blockedTag = normalizeTagName(raw);
        if (!blockedTag.isEmpty()) blocked.add(blockedTag);
      }
    }

    return catchAllBlocked ? exceptions.contains(tag) : !blocked.contains(tag);
  }

  private static String normalizeTagName(String rawTagName) {
    return Objects.toString(rawTagName, "").trim().toLowerCase(Locale.ROOT);
  }
}
