package cafe.woden.ircclient.irc.ircv3;

import java.util.List;
import java.util.Objects;

/** Immutable transport-independent outcome of one SASL session input. */
public record Ircv3SaslSessionUpdate(
    boolean complete,
    boolean requestCapability,
    List<String> rawLines,
    String startedMechanism,
    Integer successNumeric,
    Failure failure) {

  public Ircv3SaslSessionUpdate {
    rawLines = rawLines == null ? List.of() : List.copyOf(rawLines);
    startedMechanism = normalize(startedMechanism);
  }

  public static Ircv3SaslSessionUpdate active() {
    return new Ircv3SaslSessionUpdate(false, false, List.of(), null, null, null);
  }

  public static Ircv3SaslSessionUpdate completed() {
    return new Ircv3SaslSessionUpdate(true, false, List.of(), null, null, null);
  }

  public record Failure(String reason, boolean disconnect) {
    public Failure {
      reason = Objects.toString(reason, "").trim();
    }
  }

  private static String normalize(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
