package cafe.woden.ircclient.irc.ircv3;

import java.util.Locale;
import java.util.Objects;

/** Transport-neutral heuristics for identifying ZNC from CAP and RPL_MYINFO data. */
public final class Ircv3ZncDetector {

  private Ircv3ZncDetector() {}

  public static boolean seemsZncCapability(String capability) {
    String normalized = Objects.toString(capability, "").trim().toLowerCase(Locale.ROOT);
    if (normalized.startsWith("-")) normalized = normalized.substring(1);
    return normalized.startsWith("znc.in/");
  }

  /** Detects the typical ZNC version token in an RPL_MYINFO (004) line. */
  public static boolean seemsRpl004Znc(String rawLine) {
    String line = Objects.toString(rawLine, "").trim();
    if (line.isEmpty()) return false;

    String[] tokens = line.split("\\s+");
    int numericIndex = -1;
    for (int i = 0; i < tokens.length; i++) {
      if ("004".equals(tokens[i])) {
        numericIndex = i;
        break;
      }
    }
    if (numericIndex < 0) return false;

    int versionIndex = numericIndex + 3;
    if (versionIndex >= tokens.length) return false;
    return tokens[versionIndex].toLowerCase(Locale.ROOT).contains("znc");
  }
}
