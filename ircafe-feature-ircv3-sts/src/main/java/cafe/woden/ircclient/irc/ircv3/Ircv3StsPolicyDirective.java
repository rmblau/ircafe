package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;

/** Transport-independent IRCv3 STS policy directive parsed from a capability value. */
public record Ircv3StsPolicyDirective(
    long durationSeconds, Integer port, boolean preload, String rawValue) {

  public Ircv3StsPolicyDirective {
    rawValue = Objects.toString(rawValue, "").trim();
  }
}
