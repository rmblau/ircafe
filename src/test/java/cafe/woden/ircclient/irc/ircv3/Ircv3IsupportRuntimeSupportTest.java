package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3IsupportRuntimeSupportTest {

  @Test
  void adaptsOperationScopedProviderSignals() {
    Ircv3IsupportRuntimeSupport support =
        new Ircv3IsupportRuntimeSupport(
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider())));

    assertEquals(
        List.of(
            new Ircv3IsupportRuntimeSupport.TokenUpdate("MONITOR", "250", false),
            new Ircv3IsupportRuntimeSupport.TokenUpdate("WHOX", "", true)),
        support.tokenUpdates(":server 005 me MONITOR=250 -WHOX :supported"));
    assertEquals(false, support.whoxSupport("ignored").orElseThrow());

    Ircv3IsupportRuntimeSupport.MonitorSupport monitor =
        support.monitorSupport("ignored").orElseThrow();
    assertTrue(monitor.supported());
    assertEquals(250, monitor.limit());
  }

  @Test
  void ignoresBlankTokens() {
    Ircv3InboundCommandSignalProvider provider =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "test";
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(Ircv3InboundCommandOperation.ISUPPORT_TOKENS);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            return List.of(
                new Ircv3InboundCommandSignal.IsupportTokenObserved(" ", "x", false));
          }
        };

    Ircv3IsupportRuntimeSupport support =
        new Ircv3IsupportRuntimeSupport(
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider)));

    assertTrue(support.tokenUpdates("ignored").isEmpty());
  }

  private static Ircv3InboundCommandSignalProvider provider() {
    return new Ircv3InboundCommandSignalProvider() {
      @Override
      public String providerId() {
        return "test";
      }

      @Override
      public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
        return Set.of(
            Ircv3InboundCommandOperation.ISUPPORT_TOKENS,
            Ircv3InboundCommandOperation.ISUPPORT_WHOX,
            Ircv3InboundCommandOperation.ISUPPORT_MONITOR);
      }

      @Override
      public List<Ircv3InboundCommandSignal> parse(
          Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
        return switch (operation) {
          case ISUPPORT_TOKENS ->
              List.of(
                  new Ircv3InboundCommandSignal.IsupportTokenObserved(
                      "MONITOR", "250", false),
                  new Ircv3InboundCommandSignal.IsupportTokenObserved("WHOX", "", true));
          case ISUPPORT_WHOX ->
              List.of(new Ircv3InboundCommandSignal.WhoxSupportObserved(false));
          case ISUPPORT_MONITOR ->
              List.of(new Ircv3InboundCommandSignal.MonitorSupportObserved(true, 250));
          default -> List.of();
        };
      }
    };
  }
}
