package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;

/** Plans the TLS and port transport settings required by an active STS policy. */
public final class Ircv3StsTransportUpgradePlanner {

  public record Plan(int port, boolean tls, boolean changed) {}

  public Plan plan(Ircv3StsPolicy policy, int configuredPort, boolean configuredTls) {
    Objects.requireNonNull(policy, "policy");
    int port = policy.port() == null ? configuredPort : policy.port();
    boolean tls = true;
    return new Plan(port, tls, configuredPort != port || configuredTls != tls);
  }
}
