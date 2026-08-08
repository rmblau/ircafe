package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;

/** Parsed SASL failure numeric and stable user-facing disconnect reason. */
public record Ircv3SaslFailureSignal(
    int numeric, String trailingMessage, String detail, String disconnectReason) {

  public static Ircv3SaslFailureSignal parse(String rawLine) {
    Ircv3SaslIrcLine parsed = Ircv3SaslIrcLine.parse(rawLine);
    if (parsed == null || !parsed.isNumeric() || !isFailureNumeric(parsed.numeric())) {
      return null;
    }
    return from(parsed.numeric(), parsed.trailing());
  }

  public static Ircv3SaslFailureSignal from(int numeric, String rawLineOrTrailingMessage) {
    String trailing = extractTrailing(rawLineOrTrailingMessage);
    String base = baseMessage(numeric);
    String detail = base;
    if (!trailing.isEmpty() && !trailing.equalsIgnoreCase(base)) {
      detail = base + ": " + trailing;
    }
    return new Ircv3SaslFailureSignal(
        numeric, trailing.isEmpty() ? null : trailing, detail, "Login failed — " + detail);
  }

  public static boolean isFailureNumeric(int numeric) {
    return numeric == 904 || numeric == 905 || numeric == 906 || numeric == 907;
  }

  private static String baseMessage(int numeric) {
    return switch (numeric) {
      case 905 -> "SASL authentication failed (payload too long)";
      case 906 -> "SASL authentication aborted";
      case 907 -> "SASL authentication already completed";
      default -> "SASL authentication failed";
    };
  }

  private static String extractTrailing(String rawLineOrTrailingMessage) {
    String value = Objects.toString(rawLineOrTrailingMessage, "").trim();
    if (value.isEmpty()) {
      return "";
    }
    Ircv3SaslIrcLine parsed = Ircv3SaslIrcLine.parse(value);
    if (parsed != null && parsed.isNumeric()) {
      return Objects.toString(parsed.trailing(), "").trim();
    }
    return value;
  }
}
