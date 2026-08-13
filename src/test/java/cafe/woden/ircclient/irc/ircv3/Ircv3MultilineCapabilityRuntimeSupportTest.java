package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3MultilineCapabilityRuntimeSupportTest {

  @Test
  void acceptsFinalAndDraftLimitObservations() {
    Ircv3MultilineCapabilityRuntimeSupport support =
        new Ircv3MultilineCapabilityRuntimeSupport(
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider(false))));

    Ircv3MultilineCapabilityRuntimeSupport.State next =
        support.apply(Ircv3CapabilityLine.parse("ACK", ":multiline draft/multiline"), null);

    assertEquals(4096L, next.multiline().negotiatedMaxBytes());
    assertEquals(5L, next.multiline().negotiatedMaxLines());
    assertEquals(2048L, next.draftMultiline().negotiatedMaxBytes());
    assertEquals(3L, next.draftMultiline().negotiatedMaxLines());
  }

  @Test
  void rejectsDuplicateObservationsForOneCapabilityVariant() {
    Ircv3MultilineCapabilityRuntimeSupport support =
        new Ircv3MultilineCapabilityRuntimeSupport(
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider(true))));

    assertThrows(
        IllegalStateException.class,
        () -> support.apply(Ircv3CapabilityLine.parse("ACK", ":multiline"), null));
  }

  private static Ircv3InboundCommandSignalProvider provider(boolean duplicateFinal) {
    return new Ircv3InboundCommandSignalProvider() {
      @Override
      public String providerId() {
        return "multiline-test";
      }

      @Override
      public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
        return Set.of(Ircv3InboundCommandOperation.MULTILINE_CAPABILITY_STATE);
      }

      @Override
      public List<Ircv3InboundCommandSignal> parse(
          Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
        Ircv3InboundCommandSignal finalSignal =
            new Ircv3InboundCommandSignal.MultilineLimitsObserved(false, 4096L, 5L, 4096L, 5L);
        if (duplicateFinal) {
          return List.of(finalSignal, finalSignal);
        }
        return List.of(
            finalSignal,
            new Ircv3InboundCommandSignal.MultilineLimitsObserved(true, 2048L, 3L, 2048L, 3L));
      }
    };
  }
}
