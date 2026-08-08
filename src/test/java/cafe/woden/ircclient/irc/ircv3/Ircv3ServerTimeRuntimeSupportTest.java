package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3ServerTimeRuntimeSupportTest {

  @Test
  void resolvesTimestampAndPassiveLagThroughSelectedProvider() {
    Instant taggedAt = Instant.parse("2026-07-12T20:00:00Z");
    long observedAtMs = taggedAt.toEpochMilli() + 425L;
    Ircv3InboundTagSignalRuntimeCatalog inboundTags =
        Ircv3InboundTagSignalRuntimeCatalog.fromProviders(
            List.of(provider(taggedAt, 425L, observedAtMs)));
    Ircv3ServerTimeRuntimeSupport support =
        new Ircv3ServerTimeRuntimeSupport(inboundTags, emptyMessageTags(inboundTags));

    assertEquals(
        taggedAt,
        support
            .resolve(Map.of("time", "ignored-by-provider"), "@time=ignored :server PING :x")
            .orElseThrow());
    Ircv3ServerTimeRuntimeSupport.LagObservation lag =
        support.passiveLag(Map.of(), "", observedAtMs).orElseThrow();
    assertEquals(425L, lag.lagMs());
    assertEquals(observedAtMs, lag.observedAtMs());
  }

  @Test
  void ignoresMalformedInstalledProviderOutput() {
    Ircv3InboundTagSignalProvider malformed =
        new Ircv3InboundTagSignalProvider() {
          @Override
          public String providerId() {
            return "malformed";
          }

          @Override
          public Set<Ircv3InboundTagOperation> inboundTagOperations() {
            return Set.of(
                Ircv3InboundTagOperation.SERVER_TIME,
                Ircv3InboundTagOperation.SERVER_TIME_LAG);
          }

          @Override
          public List<Ircv3InboundTagSignal> parse(
              Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
            return operation == Ircv3InboundTagOperation.SERVER_TIME
                ? List.of(
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.SERVER_TIME, "not-an-instant"))
                : List.of(
                    new Ircv3InboundTagSignal(
                        Ircv3InboundTagSignalType.SERVER_TIME_LAG, "negative", "zero"));
          }
        };
    Ircv3InboundTagSignalRuntimeCatalog inboundTags =
        Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of(malformed));
    Ircv3ServerTimeRuntimeSupport support =
        new Ircv3ServerTimeRuntimeSupport(inboundTags, emptyMessageTags(inboundTags));

    assertTrue(support.resolve(Map.of(), "").isEmpty());
    assertTrue(support.passiveLag(Map.of(), "", 1L).isEmpty());
  }

  private static Ircv3MessageTagsRuntimeSupport emptyMessageTags(
      Ircv3InboundTagSignalRuntimeCatalog inboundTags) {
    return new Ircv3MessageTagsRuntimeSupport(
        Ircv3MessageTagsRuntimeCatalog.fromProviders(List.of()),
        new Ircv3MessageIdRuntimeSupport(inboundTags));
  }

  private static Ircv3InboundTagSignalProvider provider(
      Instant taggedAt, long lagMs, long observedAtMs) {
    return new Ircv3InboundTagSignalProvider() {
      @Override
      public String providerId() {
        return "plugin-server-time";
      }

      @Override
      public Set<Ircv3InboundTagOperation> inboundTagOperations() {
        return Set.of(
            Ircv3InboundTagOperation.SERVER_TIME,
            Ircv3InboundTagOperation.SERVER_TIME_LAG);
      }

      @Override
      public List<Ircv3InboundTagSignal> parse(
          Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
        return switch (operation) {
          case SERVER_TIME ->
              List.of(
                  Ircv3InboundTagSignal.of(
                      Ircv3InboundTagSignalType.SERVER_TIME, taggedAt.toString()));
          case SERVER_TIME_LAG ->
              List.of(
                  new Ircv3InboundTagSignal(
                      Ircv3InboundTagSignalType.SERVER_TIME_LAG,
                      Long.toString(lagMs),
                      Long.toString(observedAtMs)));
          default -> List.of();
        };
      }
    };
  }
}
