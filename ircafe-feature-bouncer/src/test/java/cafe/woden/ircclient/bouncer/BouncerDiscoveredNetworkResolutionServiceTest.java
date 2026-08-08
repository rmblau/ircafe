package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerEphemeralServerSpec;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingContext;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BouncerDiscoveredNetworkResolutionServiceTest {

  private final BouncerDiscoveredNetworkResolutionService service =
      new BouncerDiscoveredNetworkResolutionService();

  @Test
  void resolvesNetworkThenBuildsServerSpecWithResolvedServerChannels() {
    RecordingStrategy strategy = new RecordingStrategy();
    BouncerServerProfile profile = new BouncerServerProfile("bouncer-1", "base-user", "sasl-user");
    BouncerDiscoveredNetwork network =
        new BouncerDiscoveredNetwork(
            "generic", "bouncer-1", "libera", "Libera", "Libera", Map.of());
    BouncerNetworkMappingContext context =
        new BouncerNetworkMappingContext("${login}/${network}", true);

    BouncerDiscoveredNetworkResolution resolution =
        service.resolve(
            strategy,
            profile,
            network,
            context,
            serverId -> {
              assertEquals("bouncer:bouncer-1:libera", serverId);
              return List.of("#one", "#two");
            });

    assertSame(profile, strategy.profile);
    assertSame(network, strategy.network);
    assertSame(context, strategy.context);
    assertEquals(strategy.resolved, resolution.resolvedNetwork());
    assertEquals(strategy.spec, resolution.serverSpec());
    assertEquals(List.of("#one", "#two"), strategy.autoJoinChannels);
  }


  @Test
  void treatsNullAutoJoinChannelResultAsEmptyList() {
    RecordingStrategy strategy = new RecordingStrategy();
    BouncerServerProfile profile = new BouncerServerProfile("bouncer-1", "login", "sasl");
    BouncerDiscoveredNetwork network =
        new BouncerDiscoveredNetwork(
            "generic", "bouncer-1", "libera", "Libera", "Libera", Map.of());

    service.resolve(
        strategy, profile, network, BouncerNetworkMappingContext.defaults(), serverId -> null);

    assertEquals(List.of(), strategy.autoJoinChannels);
  }

  @Test
  void rejectsMissingInputs() {
    RecordingStrategy strategy = new RecordingStrategy();
    BouncerServerProfile profile = new BouncerServerProfile("bouncer-1", "login", "sasl");
    BouncerDiscoveredNetwork network =
        new BouncerDiscoveredNetwork(
            "generic", "bouncer-1", "libera", "Libera", "Libera", Map.of());
    BouncerNetworkMappingContext context = BouncerNetworkMappingContext.defaults();

    assertThrows(
        NullPointerException.class,
        () -> service.resolve(null, profile, network, context, serverId -> List.of()));
    assertThrows(
        NullPointerException.class,
        () -> service.resolve(strategy, null, network, context, serverId -> List.of()));
    assertThrows(
        NullPointerException.class,
        () -> service.resolve(strategy, profile, null, context, serverId -> List.of()));
    assertThrows(
        NullPointerException.class,
        () -> service.resolve(strategy, profile, network, null, serverId -> List.of()));
    assertThrows(
        NullPointerException.class,
        () -> service.resolve(strategy, profile, network, context, null));
  }

  @Test
  void rejectsNullStrategyResults() {
    BouncerServerProfile profile = new BouncerServerProfile("bouncer-1", "login", "sasl");
    BouncerDiscoveredNetwork network =
        new BouncerDiscoveredNetwork(
            "generic", "bouncer-1", "libera", "Libera", "Libera", Map.of());
    BouncerNetworkMappingContext context = BouncerNetworkMappingContext.defaults();

    assertThrows(
        NullPointerException.class,
        () ->
            service.resolve(
                new NullResolvedStrategy(), profile, network, context, serverId -> List.of()));
    assertThrows(
        NullPointerException.class,
        () ->
            service.resolve(
                new NullSpecStrategy(), profile, network, context, serverId -> List.of()));
  }

  private static class RecordingStrategy implements BouncerNetworkMappingStrategy {
    private final ResolvedBouncerNetwork resolved =
        new ResolvedBouncerNetwork(
            "bouncer:bouncer-1:libera", "base-user/Libera", "Libera", "Libera");
    private final BouncerEphemeralServerSpec spec =
        new BouncerEphemeralServerSpec(
            "bouncer:bouncer-1:libera", "base-user/Libera", List.of("#one", "#two"));

    private BouncerServerProfile profile;
    private BouncerDiscoveredNetwork network;
    private BouncerNetworkMappingContext context;
    private List<String> autoJoinChannels;

    @Override
    public String backendId() {
      return "generic";
    }

    @Override
    public Set<String> capabilityHints() {
      return Set.of("network-discovery");
    }

    @Override
    public ResolvedBouncerNetwork resolveNetwork(
        BouncerServerProfile bouncer,
        BouncerDiscoveredNetwork network,
        BouncerNetworkMappingContext context) {
      this.profile = bouncer;
      this.network = network;
      this.context = context;
      return resolved;
    }

    @Override
    public BouncerEphemeralServerSpec buildEphemeralServer(
        BouncerServerProfile bouncer,
        ResolvedBouncerNetwork resolved,
        List<String> autoJoinChannels) {
      this.autoJoinChannels = autoJoinChannels;
      assertSame(profile, bouncer);
      assertSame(this.resolved, resolved);
      return spec;
    }
  }

  private static final class NullResolvedStrategy implements BouncerNetworkMappingStrategy {
    @Override
    public String backendId() {
      return "generic";
    }

    @Override
    public ResolvedBouncerNetwork resolveNetwork(
        BouncerServerProfile bouncer,
        BouncerDiscoveredNetwork network,
        BouncerNetworkMappingContext context) {
      return null;
    }
  }

  private static final class NullSpecStrategy extends RecordingStrategy {
    @Override
    public BouncerEphemeralServerSpec buildEphemeralServer(
        BouncerServerProfile bouncer,
        ResolvedBouncerNetwork resolved,
        List<String> autoJoinChannels) {
      return null;
    }
  }
}
