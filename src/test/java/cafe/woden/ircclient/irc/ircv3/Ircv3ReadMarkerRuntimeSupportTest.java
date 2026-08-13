package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3ReadMarkerRuntimeSupportTest {

  @Test
  void builtInProviderRendersAndObservesReadMarkers() {
    Ircv3ReadMarkerRuntimeSupport support = Ircv3RuntimeTestFixtures.runtime().readMarker();

    Ircv3ReadMarkerRuntimeSupport.OutboundPlan plan =
        support.render("#ircafe", Instant.parse("2026-07-13T12:34:56Z"));
    Ircv3ReadMarkerRuntimeSupport.TagObservation tagged =
        support
            .fromTags(
                new Ircv3InboundTagRequest(
                    "TAGMSG",
                    "alice",
                    "#ircafe",
                    List.of("#ircafe"),
                    Map.of("draft/read-marker", "timestamp=2026-07-13T12:34:56Z")))
            .orElseThrow();
    Ircv3ReadMarkerRuntimeSupport.CommandObservation command =
        support
            .fromCommand(
                new Ircv3InboundCommandRequest(
                    "server",
                    "MARKREAD",
                    ":server MARKREAD #ircafe timestamp=2026-07-13T12:34:56Z",
                    List.of("#ircafe", "timestamp=2026-07-13T12:34:56Z"),
                    Map.of()))
            .orElseThrow();

    assertTrue(support.outboundAvailable());
    assertEquals("MARKREAD #ircafe timestamp=2026-07-13T12:34:56.000Z", plan.rawLine());
    assertEquals("#ircafe", plan.target());
    assertEquals("timestamp=2026-07-13T12:34:56.000Z", plan.marker());
    assertEquals("timestamp=2026-07-13T12:34:56Z", tagged.marker());
    assertEquals("#ircafe", command.target());
    assertEquals("timestamp=2026-07-13T12:34:56Z", command.marker());
  }

  @Test
  void replacementProvidersCanOwnEachRuntimeSeam() {
    Ircv3OutboundCommandProvider outbound =
        new Ircv3OutboundCommandProvider() {
          @Override
          public String providerId() {
            return "plugin-read-marker";
          }

          @Override
          public Set<Ircv3OutboundCommandOperation> operations() {
            return Set.of(Ircv3OutboundCommandOperation.READ_MARKER);
          }

          @Override
          public List<String> build(
              Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
            return List.of("MARKREAD " + request.target() + " timestamp=2026-07-13T13:00:00Z");
          }
        };
    Ircv3InboundTagSignalProvider tagged =
        new Ircv3InboundTagSignalProvider() {
          @Override
          public String providerId() {
            return "plugin-read-marker";
          }

          @Override
          public Set<Ircv3InboundTagOperation> inboundTagOperations() {
            return Set.of(Ircv3InboundTagOperation.READ_MARKER);
          }

          @Override
          public List<Ircv3InboundTagSignal> parse(
              Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
            return List.of(
                Ircv3InboundTagSignal.of(
                    Ircv3InboundTagSignalType.READ_MARKER, "timestamp=plugin-tag"));
          }
        };
    Ircv3InboundCommandSignalProvider command =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "plugin-read-marker";
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(Ircv3InboundCommandOperation.READ_MARKER);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            return List.of(
                new Ircv3InboundCommandSignal.ReadMarkerObserved(
                    "#plugin", "timestamp=plugin-command"));
          }
        };
    Ircv3ReadMarkerRuntimeSupport support =
        new Ircv3ReadMarkerRuntimeSupport(
            Ircv3OutboundCommandRuntimeCatalog.fromProviders(List.of(outbound)),
            Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of(tagged)),
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(command)));

    assertEquals(
        "timestamp=2026-07-13T13:00:00Z", support.render("#ircafe", Instant.EPOCH).marker());
    assertEquals(
        "timestamp=plugin-tag",
        support
            .fromTags(new Ircv3InboundTagRequest("TAGMSG", "", "", List.of(), Map.of()))
            .orElseThrow()
            .marker());
    assertEquals(
        new Ircv3ReadMarkerRuntimeSupport.CommandObservation("#plugin", "timestamp=plugin-command"),
        support
            .fromCommand(new Ircv3InboundCommandRequest("", "MARKREAD", "", List.of(), Map.of()))
            .orElseThrow());
  }

  @Test
  void rejectsUnsafeOrAmbiguousProviderOutput() {
    Ircv3OutboundCommandProvider outbound =
        new Ircv3OutboundCommandProvider() {
          @Override
          public String providerId() {
            return "unsafe-read-marker";
          }

          @Override
          public Set<Ircv3OutboundCommandOperation> operations() {
            return Set.of(Ircv3OutboundCommandOperation.READ_MARKER);
          }

          @Override
          public List<String> build(
              Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
            return List.of("MARKREAD #other timestamp=2026-07-13T12:34:56Z");
          }
        };
    Ircv3InboundTagSignalProvider tagged =
        new Ircv3InboundTagSignalProvider() {
          @Override
          public String providerId() {
            return "ambiguous-read-marker";
          }

          @Override
          public Set<Ircv3InboundTagOperation> inboundTagOperations() {
            return Set.of(Ircv3InboundTagOperation.READ_MARKER);
          }

          @Override
          public List<Ircv3InboundTagSignal> parse(
              Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
            return List.of(
                Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.READ_MARKER, "one"),
                Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.READ_MARKER, "two"));
          }
        };
    Ircv3InboundCommandSignalProvider command =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "unsafe-read-marker";
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(Ircv3InboundCommandOperation.READ_MARKER);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            return List.of(
                new Ircv3InboundCommandSignal.ReadMarkerObserved(
                    "#ircafe\r\nQUIT", "timestamp=unsafe"));
          }
        };
    Ircv3ReadMarkerRuntimeSupport support =
        new Ircv3ReadMarkerRuntimeSupport(
            Ircv3OutboundCommandRuntimeCatalog.fromProviders(List.of(outbound)),
            Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of(tagged)),
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(command)));

    assertThrows(
        IllegalStateException.class,
        () -> support.render("#ircafe", Instant.parse("2026-07-13T12:34:56Z")));
    assertFalse(
        support
            .fromTags(new Ircv3InboundTagRequest("TAGMSG", "", "", List.of(), Map.of()))
            .isPresent());
    assertFalse(
        support
            .fromCommand(new Ircv3InboundCommandRequest("", "MARKREAD", "", List.of(), Map.of()))
            .isPresent());
  }
}
