package cafe.woden.ircclient.plugin.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingContext;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BouncerNetworkMappingContextTest {

  @Test
  void normalizesBlankLoginTemplateToDefault() {
    BouncerNetworkMappingContext context = new BouncerNetworkMappingContext("  ", false);

    assertEquals(
        BouncerNetworkMappingContext.DEFAULT_GENERIC_LOGIN_TEMPLATE,
        context.genericLoginTemplate());
    assertEquals(false, context.preferLoginHint());
  }

  @Test
  void supportsContextAwareMappingStrategies() {
    BouncerNetworkMappingStrategy strategy =
        new BouncerNetworkMappingStrategy() {
          @Override
          public String backendId() {
            return "context";
          }

          @Override
          public ResolvedBouncerNetwork resolveNetwork(
              BouncerServerProfile bouncer,
              BouncerDiscoveredNetwork network,
              BouncerNetworkMappingContext context) {
            return new ResolvedBouncerNetwork(
                "context:" + network.networkId(),
                context.genericLoginTemplate(),
                network.displayName(),
                network.autoConnectName());
          }
        };

    ResolvedBouncerNetwork resolved =
        strategy.resolveNetwork(
            new BouncerServerProfile("origin", "login", "sasl"),
            network(),
            new BouncerNetworkMappingContext("{base}|{network}", true));

    assertEquals("context:libera", resolved.serverId());
    assertEquals("{base}|{network}", resolved.loginUser());
  }

  @Test
  void adaptsLegacyRequestOnlyMappingStrategiesToContextCalls() {
    BouncerNetworkMappingStrategy strategy =
        new BouncerNetworkMappingStrategy() {
          @Override
          public String backendId() {
            return "legacy";
          }

          @Override
          public ResolvedBouncerNetwork resolveNetwork(
              BouncerServerProfile bouncer, BouncerDiscoveredNetwork network) {
            return new ResolvedBouncerNetwork(
                "legacy:" + network.networkId(),
                bouncer.preferredLoginUser(),
                network.displayName(),
                network.autoConnectName());
          }
        };

    ResolvedBouncerNetwork resolved =
        strategy.resolveNetwork(
            new BouncerServerProfile("origin", "login", "sasl"),
            network(),
            BouncerNetworkMappingContext.defaults());

    assertEquals("legacy:libera", resolved.serverId());
    assertEquals("sasl", resolved.loginUser());
  }

  @Test
  void requestOnlyDefaultExplainsRequiredImplementationChoice() {
    BouncerNetworkMappingStrategy strategy = () -> "incomplete";

    UnsupportedOperationException error =
        assertThrows(
            UnsupportedOperationException.class,
            () ->
                strategy.resolveNetwork(
                    new BouncerServerProfile("origin", "login", "sasl"), network()));

    assertEquals(
        "Bouncer mapping strategies must implement resolveNetwork(bouncer, network) "
            + "or resolveNetwork(bouncer, network, context).",
        error.getMessage());
  }

  private static BouncerDiscoveredNetwork network() {
    return new BouncerDiscoveredNetwork(
        "context", "origin", "libera", "Libera", "Libera", Map.of());
  }
}
