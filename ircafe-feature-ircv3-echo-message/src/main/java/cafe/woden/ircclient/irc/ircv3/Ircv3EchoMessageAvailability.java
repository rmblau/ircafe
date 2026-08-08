package cafe.woden.ircclient.irc.ircv3;

/** Transport-independent echo-message readiness policy. */
public final class Ircv3EchoMessageAvailability {

  private Ircv3EchoMessageAvailability() {}

  public static boolean isAvailable(boolean sessionLive, boolean echoMessageNegotiated) {
    return sessionLive && echoMessageNegotiated;
  }
}
