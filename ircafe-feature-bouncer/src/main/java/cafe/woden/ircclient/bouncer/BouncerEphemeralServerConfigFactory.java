package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerEphemeralServerSpec;
import java.util.Objects;

/** Feature-owned factory for applying mapping-strategy specs to configured bouncer fields. */
public final class BouncerEphemeralServerConfigFactory {

  public BouncerEphemeralServerConfig fromConfiguredServer(
      BouncerConfiguredServerTemplate configured, BouncerEphemeralServerSpec spec) {
    Objects.requireNonNull(configured, "configured");
    Objects.requireNonNull(spec, "spec");

    BouncerConfiguredServerTemplate.Sasl sasl = configured.sasl();
    return new BouncerEphemeralServerConfig(
        spec.serverId(),
        configured.host(),
        configured.port(),
        configured.tls(),
        configured.serverPassword(),
        configured.nick(),
        spec.loginUser(),
        configured.realName(),
        new BouncerEphemeralServerConfig.Sasl(
            sasl.enabled(),
            spec.loginUser(),
            sasl.password(),
            sasl.mechanism(),
            sasl.disconnectOnFailure()),
        spec.autoJoinChannels());
  }
}
