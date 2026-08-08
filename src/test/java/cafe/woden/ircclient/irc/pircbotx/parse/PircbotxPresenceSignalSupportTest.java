package cafe.woden.ircclient.irc.pircbotx.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.IrcEvent;
import cafe.woden.ircclient.irc.ServerIrcEvent;
import cafe.woden.ircclient.irc.ircv3.Ircv3InboundCommandSignalRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PircbotxPresenceSignalSupportTest {

  @Test
  void awayNotifyEmitsAwayStateAndHostmask() {
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxPresenceSignalSupport support =
        PircbotxParserRuntimeTestFixtures.presence("libera", out::add);

    support.observe(
        Instant.parse("2026-03-23T12:10:00Z"),
        "alice",
        "AWAY",
        ":alice!u@h AWAY :Gone away",
        List.of(":Gone away"));

    assertTrue(
        out.stream()
            .map(ServerIrcEvent::event)
            .anyMatch(
                e ->
                    e instanceof IrcEvent.UserHostmaskObserved hm
                        && "alice".equals(hm.nick())
                        && "alice!u@h".equals(hm.hostmask())));
    IrcEvent.UserAwayStateObserved away =
        out.stream()
            .map(ServerIrcEvent::event)
            .filter(IrcEvent.UserAwayStateObserved.class::isInstance)
            .map(IrcEvent.UserAwayStateObserved.class::cast)
            .findFirst()
            .orElseThrow();
    assertEquals(IrcEvent.AwayState.AWAY, away.awayState());
    assertEquals("Gone away", away.awayMessage());
  }

  @Test
  void accountNotifyMapsStarToLoggedOut() {
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxPresenceSignalSupport support =
        PircbotxParserRuntimeTestFixtures.presence("libera", out::add);

    support.observe(
        Instant.parse("2026-03-23T12:15:00Z"),
        "alice",
        "ACCOUNT",
        ":alice!u@h ACCOUNT *",
        List.of("*"));

    IrcEvent.UserAccountStateObserved account =
        out.stream()
            .map(ServerIrcEvent::event)
            .filter(IrcEvent.UserAccountStateObserved.class::isInstance)
            .map(IrcEvent.UserAccountStateObserved.class::cast)
            .findFirst()
            .orElseThrow();
    assertEquals(IrcEvent.AccountState.LOGGED_OUT, account.accountState());
    assertNull(account.accountName());
  }

  @Test
  void extendedJoinEmitsAccountAndRealNameSignals() {
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxPresenceSignalSupport support =
        PircbotxParserRuntimeTestFixtures.presence("libera", out::add);

    support.observe(
        Instant.parse("2026-03-23T12:20:00Z"),
        "alice",
        "JOIN",
        ":alice!u@h JOIN #ircafe alice-account :Alice Liddell",
        List.of("#ircafe", "alice-account", ":Alice Liddell"));

    assertTrue(
        out.stream()
            .map(ServerIrcEvent::event)
            .anyMatch(
                e ->
                    e instanceof IrcEvent.UserAccountStateObserved ac
                        && "alice".equals(ac.nick())
                        && IrcEvent.AccountState.LOGGED_IN == ac.accountState()
                        && "alice-account".equals(ac.accountName())));
    assertTrue(
        out.stream()
            .map(ServerIrcEvent::event)
            .anyMatch(
                e ->
                    e instanceof IrcEvent.UserSetNameObserved sn
                        && "alice".equals(sn.nick())
                        && "Alice Liddell".equals(sn.realName())
                        && sn.source() == IrcEvent.UserSetNameObserved.Source.EXTENDED_JOIN));
  }

  @Test
  void runtimeProviderCanOverrideIdentityChangeInterpretation() {
    Ircv3InboundCommandSignalProvider provider =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "test-presence";
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(Ircv3InboundCommandOperation.SETNAME);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            return List.of(
                new Ircv3InboundCommandSignal.SetNameObserved(
                    request.sourceNick(),
                    "",
                    "Plugin Alice",
                    Ircv3InboundCommandSignal.SetNameSource.SETNAME));
          }
        };
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxPresenceSignalSupport support =
        new PircbotxPresenceSignalSupport(
            "libera",
            out::add,
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider)));

    assertTrue(
        support.observeIdentityChange(
            Instant.parse("2026-03-23T12:25:00Z"),
            "alice",
            "SETNAME",
            ":alice SETNAME :Alice Liddell",
            List.of(":Alice Liddell")));

    IrcEvent.UserSetNameObserved observed =
        out.stream()
            .map(ServerIrcEvent::event)
            .filter(IrcEvent.UserSetNameObserved.class::isInstance)
            .map(IrcEvent.UserSetNameObserved.class::cast)
            .findFirst()
            .orElseThrow();
    assertEquals("Plugin Alice", observed.realName());
  }

  @Test
  void focusedOperationWinsWhenLegacyProviderAlsoResponds() {
    Ircv3InboundCommandSignalProvider focusedProvider =
        providerForSetName("focused", Ircv3InboundCommandOperation.SETNAME, "Focused Alice");
    Ircv3InboundCommandSignalProvider legacyProvider =
        providerForSetName(
            "legacy", Ircv3InboundCommandOperation.IDENTITY_CHANGE, "Legacy Alice");
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxPresenceSignalSupport support =
        new PircbotxPresenceSignalSupport(
            "libera",
            out::add,
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(
                List.of(focusedProvider, legacyProvider)));

    assertTrue(
        support.observeIdentityChange(
            Instant.parse("2026-03-23T12:27:00Z"),
            "alice",
            "SETNAME",
            ":alice SETNAME :Alice Liddell",
            List.of(":Alice Liddell")));

    IrcEvent.UserSetNameObserved observed =
        assertEvent(out, IrcEvent.UserSetNameObserved.class);
    assertEquals("Focused Alice", observed.realName());
  }

  @Test
  void rawAwayFallbackEmitsThroughRuntimeProvider() {
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxPresenceSignalSupport support =
        PircbotxParserRuntimeTestFixtures.presence("libera", out::add);

    assertTrue(
        support.observeAwayNotifyRawLine(
            Instant.parse("2026-03-23T12:30:00Z"),
            ":alice!u@h AWAY :Fallback away"));

    IrcEvent.UserAwayStateObserved away =
        assertEvent(out, IrcEvent.UserAwayStateObserved.class);
    assertEquals("alice", away.nick());
    assertEquals(IrcEvent.AwayState.AWAY, away.awayState());
    assertEquals("Fallback away", away.awayMessage());
  }

  @Test
  void selfAwayConfirmationEmitsDefaultMessageWhenServerOmitsOne() {
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxPresenceSignalSupport support =
        PircbotxParserRuntimeTestFixtures.presence("libera", out::add);

    assertTrue(
        support.observeSelfAwayConfirmation(
            Instant.parse("2026-03-23T12:35:00Z"), 305, ":server 305 me"));

    IrcEvent.AwayStatusChanged away = assertEvent(out, IrcEvent.AwayStatusChanged.class);
    assertEquals(false, away.away());
    assertEquals("You are no longer marked as being away", away.message());
  }

  @Test
  void runtimeProviderCanOverrideSelfAwayFallbackInterpretation() {
    Ircv3InboundCommandSignalProvider provider =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "test-presence";
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(Ircv3InboundCommandOperation.AWAY_NOTIFY);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            return List.of(
                new Ircv3InboundCommandSignal.SelfAwayObserved(
                    false, "plugin.example", "Plugin says you are here"));
          }
        };
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxPresenceSignalSupport support =
        new PircbotxPresenceSignalSupport(
            "libera",
            out::add,
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider)));

    assertTrue(
        support.observeSelfAwayConfirmationRawLine(
            Instant.parse("2026-03-23T12:40:00Z"), ":server 306 me :ignored"));

    IrcEvent.AwayStatusChanged away = assertEvent(out, IrcEvent.AwayStatusChanged.class);
    assertEquals(false, away.away());
    assertEquals("Plugin says you are here", away.message());
  }

  @Test
  void rawFallbackFiltersSignalsThatDoNotMatchTheRequestedObservation() {
    Ircv3InboundCommandSignalProvider provider =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "test-presence";
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(Ircv3InboundCommandOperation.PRESENCE);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            return List.of(
                new Ircv3InboundCommandSignal.UserAwayObserved("alice", true, "Away"),
                new Ircv3InboundCommandSignal.SelfAwayObserved(false, "server", "Here"));
          }
        };
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxPresenceSignalSupport support =
        new PircbotxPresenceSignalSupport(
            "libera",
            out::add,
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider)));

    assertTrue(
        support.observeAwayNotifyRawLine(
            Instant.parse("2026-03-23T12:45:00Z"), ":alice!u@h AWAY :Away"));

    assertEquals(1, out.size());
    assertTrue(out.getFirst().event() instanceof IrcEvent.UserAwayStateObserved);
  }

  private static Ircv3InboundCommandSignalProvider providerForSetName(
      String providerId, Ircv3InboundCommandOperation operation, String realName) {
    return new Ircv3InboundCommandSignalProvider() {
      @Override
      public String providerId() {
        return providerId;
      }

      @Override
      public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
        return Set.of(operation);
      }

      @Override
      public List<Ircv3InboundCommandSignal> parse(
          Ircv3InboundCommandOperation requestedOperation,
          Ircv3InboundCommandRequest request) {
        return List.of(
            new Ircv3InboundCommandSignal.SetNameObserved(
                request.sourceNick(),
                "",
                realName,
                Ircv3InboundCommandSignal.SetNameSource.SETNAME));
      }
    };
  }

  private static <T extends IrcEvent> T assertEvent(
      List<ServerIrcEvent> events, Class<T> type) {
    return events.stream()
        .map(ServerIrcEvent::event)
        .filter(type::isInstance)
        .map(type::cast)
        .findFirst()
        .orElseThrow();
  }
}
