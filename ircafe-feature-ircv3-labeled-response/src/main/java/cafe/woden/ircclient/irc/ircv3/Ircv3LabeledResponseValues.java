package cafe.woden.ircclient.irc.ircv3;

import java.util.Locale;
import java.util.Objects;

/** Normalization and client-label generation policy for IRCv3 labeled-response. */
public final class Ircv3LabeledResponseValues {

  private Ircv3LabeledResponseValues() {}

  public static String normalizeServer(String serverId) {
    return Objects.toString(serverId, "").trim();
  }

  public static String normalizeLabel(String label) {
    return Objects.toString(label, "").trim();
  }

  public static String generateClientLabel(String serverId, long sequence) {
    String sid = normalizeServer(serverId).toLowerCase(Locale.ROOT);
    StringBuilder compact = new StringBuilder(Math.max(4, sid.length()));
    for (int i = 0; i < sid.length(); i++) {
      char c = sid.charAt(i);
      if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '_') {
        compact.append(c);
      }
    }
    if (compact.isEmpty()) compact.append("srv");
    return "ircafe-" + compact + "-" + Long.toString(sequence, 36);
  }
}
