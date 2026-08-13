package cafe.woden.ircclient.irc.ircv3;

/** Capability-owned failure raised while negotiating or computing an IRCv3 SASL exchange. */
public final class Ircv3SaslException extends Exception {

  public enum Reason {
    SASL_FAILED,
    UNSUPPORTED_MECHANISM,
    OTHER
  }

  private final Reason reason;

  public Ircv3SaslException(Reason reason, String message) {
    super(message);
    this.reason = reason == null ? Reason.OTHER : reason;
  }

  public Ircv3SaslException(Reason reason, String message, Throwable cause) {
    super(message, cause);
    this.reason = reason == null ? Reason.OTHER : reason;
  }

  public Reason reason() {
    return reason;
  }
}
