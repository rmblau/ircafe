package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.Objects;

/** A transport-neutral value emitted by an inbound IRCv3 tag provider. */
public record Ircv3InboundTagSignal(
    Ircv3InboundTagSignalType type, String primaryValue, String secondaryValue) {

  public Ircv3InboundTagSignal {
    type = Objects.requireNonNull(type, "type");
    primaryValue = Objects.toString(primaryValue, "").trim();
    secondaryValue = Objects.toString(secondaryValue, "").trim();
  }

  public static Ircv3InboundTagSignal of(Ircv3InboundTagSignalType type, String primaryValue) {
    return new Ircv3InboundTagSignal(type, primaryValue, "");
  }
}
