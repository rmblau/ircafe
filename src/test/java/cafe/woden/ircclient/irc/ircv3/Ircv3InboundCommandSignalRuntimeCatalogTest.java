package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.InstalledPluginProblem;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3InboundCommandSignalRuntimeCatalogTest {

  @Test
  void loadsFocusedBuiltInRuntimeProvidersFromApplicationClasspath() {
    Ircv3InboundCommandSignalRuntimeCatalog catalog =
        Ircv3InboundCommandSignalRuntimeCatalog.applicationClasspath();

    assertEquals(
        List.of(
            "away-notify",
            "account-notify",
            "extended-join",
            "chghost",
            "setname",
            "invite-notify",
            "standard-replies",
            "monitor",
            "user-identity",
            "read-marker",
            "message-redaction",
            "batch",
            "znc-playback",
            "multiline",
            "negotiation",
            "typing",
            "sts",
            "sasl"),
        catalog.providerIds());
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.AWAY_NOTIFY));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.ACCOUNT_NOTIFY));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.EXTENDED_JOIN));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.CHGHOST));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.SETNAME));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.INVITE_NOTIFY));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.STANDARD_REPLY));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.MONITOR));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.USERHOST));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.WHOIS_AWAY));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.WHOIS_ACCOUNT));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.WHOIS_END));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.WHOIS_USER));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.WHO));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.WHOX));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.READ_MARKER));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.MESSAGE_REDACTION));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.HISTORY_BATCH_CONTROL));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.HISTORY_ZNC_CAPABILITY));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.HISTORY_ZNC_RPL004));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.MULTILINE_CAPABILITY_STATE));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.CAP_NEGOTIATION));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.ISUPPORT_TOKENS));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.ISUPPORT_WHOX));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.ISUPPORT_MONITOR));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.ISUPPORT_CLIENT_TAG_POLICY));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.STS_CAPABILITY));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.SASL_CAPABILITY_LIST));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.SASL_CAPABILITY_ACK));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.SASL_CAPABILITY_NAK));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.SASL_SERVER_MESSAGE));
    assertTrue(catalog.supports(Ircv3InboundCommandOperation.SASL_FAILURE));

    assertEquals(
        List.of(
            Ircv3InboundCommandSignal.HostmaskObserved.class,
            Ircv3InboundCommandSignal.UserAwayObserved.class),
        catalog
            .parse(
                Ircv3InboundCommandOperation.AWAY_NOTIFY,
                new Ircv3InboundCommandRequest(
                    "alice", "AWAY", ":alice!u@h AWAY :Gone away", List.of(":Gone away"), Map.of()))
            .stream()
            .map(Object::getClass)
            .toList());

    assertEquals(
        new Ircv3InboundCommandSignal.UserAwayObserved("alice", true, "Fallback away"),
        catalog
            .parse(
                Ircv3InboundCommandOperation.AWAY_NOTIFY,
                new Ircv3InboundCommandRequest(
                    "", "AWAY", ":alice!u@h AWAY :Fallback away", List.of(), Map.of()))
            .getFirst());

    assertEquals(
        new Ircv3InboundCommandSignal.SelfAwayObserved(
            true, "server", "You have been marked as being away"),
        catalog
            .parse(
                Ircv3InboundCommandOperation.AWAY_NOTIFY,
                new Ircv3InboundCommandRequest(
                    "",
                    "306",
                    ":server 306 me :You have been marked as being away",
                    List.of(),
                    Map.of()))
            .getFirst());

    assertEquals(
        List.of(
            new Ircv3InboundCommandSignal.HostmaskObserved("alice", "alice!u@h"),
            new Ircv3InboundCommandSignal.AccountObserved(
                "alice", Ircv3InboundCommandSignal.AccountState.LOGGED_IN, "alice-account")),
        catalog.parse(
            Ircv3InboundCommandOperation.ACCOUNT_NOTIFY,
            new Ircv3InboundCommandRequest(
                "alice",
                "ACCOUNT",
                ":alice!u@h ACCOUNT alice-account",
                List.of("alice-account"),
                Map.of())));

    assertEquals(
        List.of(
            new Ircv3InboundCommandSignal.AccountObserved(
                "alice", Ircv3InboundCommandSignal.AccountState.LOGGED_IN, "alice-account"),
            new Ircv3InboundCommandSignal.SetNameObserved(
                "alice",
                "#ircafe",
                "Alice Liddell",
                Ircv3InboundCommandSignal.SetNameSource.EXTENDED_JOIN)),
        catalog.parse(
            Ircv3InboundCommandOperation.EXTENDED_JOIN,
            new Ircv3InboundCommandRequest(
                "alice",
                "JOIN",
                ":alice!u@h JOIN #ircafe alice-account :Alice Liddell",
                List.of("#ircafe", "alice-account", ":Alice Liddell"),
                Map.of())));

    assertEquals(
        new Ircv3InboundCommandSignal.HostChangedObserved(
            "alice", "newuser", "new.host", "alice!newuser@new.host"),
        catalog
            .parse(
                Ircv3InboundCommandOperation.CHGHOST,
                new Ircv3InboundCommandRequest(
                    "alice",
                    "CHGHOST",
                    ":alice!u@h CHGHOST newuser new.host",
                    List.of("newuser", "new.host"),
                    Map.of()))
            .getFirst());

    assertEquals(
        new Ircv3InboundCommandSignal.SetNameObserved(
            "alice", "", "Alice Liddell", Ircv3InboundCommandSignal.SetNameSource.SETNAME),
        catalog
            .parse(
                Ircv3InboundCommandOperation.SETNAME,
                new Ircv3InboundCommandRequest(
                    "alice",
                    "SETNAME",
                    ":alice!u@h SETNAME :Alice Liddell",
                    List.of(":Alice Liddell"),
                    Map.of()))
            .getFirst());

    assertEquals(
        new Ircv3InboundCommandSignal.InviteObserved("alice", "me", "#ircafe", "join us"),
        catalog
            .parse(
                Ircv3InboundCommandOperation.INVITE_NOTIFY,
                new Ircv3InboundCommandRequest(
                    "alice",
                    "INVITE",
                    ":alice!u@h INVITE me #ircafe :join us",
                    List.of("me", "#ircafe"),
                    Map.of()))
            .getFirst());

    assertTrue(
        catalog
                .parse(
                    Ircv3InboundCommandOperation.STANDARD_REPLY,
                    new Ircv3InboundCommandRequest(
                        "server",
                        "FAIL",
                        ":server FAIL CHATHISTORY INVALID_PARAMS :Bad selector",
                        List.of("CHATHISTORY", "INVALID_PARAMS", ":Bad selector"),
                        Map.of()))
                .getFirst()
            instanceof Ircv3InboundCommandSignal.StandardReplyObserved);

    assertEquals(
        new Ircv3InboundCommandSignal.MonitorStatusObserved(
            true,
            List.of(new Ircv3InboundCommandSignal.MonitorStatusEntry("Alice", "Alice!u@host"))),
        catalog
            .parse(
                Ircv3InboundCommandOperation.MONITOR,
                new Ircv3InboundCommandRequest(
                    "server",
                    "730",
                    ":server 730 me :Alice!u@host",
                    List.of("me", ":Alice!u@host"),
                    Map.of()))
            .getFirst());

    List<Ircv3InboundCommandSignal> userhostSignals =
        catalog.parse(
            Ircv3InboundCommandOperation.USERHOST,
            new Ircv3InboundCommandRequest(
                "server",
                "302",
                ":server 302 me :alice=+ident@host.example",
                List.of("me", ":alice=+ident@host.example"),
                Map.of()));
    assertEquals(2, userhostSignals.size());
    assertEquals(
        new Ircv3InboundCommandSignal.HostmaskObserved("alice", "alice!ident@host.example"),
        userhostSignals.get(0));
    assertEquals(
        new Ircv3InboundCommandSignal.UserAwayObserved("alice", false, null),
        userhostSignals.get(1));

    assertEquals(
        new Ircv3InboundCommandSignal.WhoisEndedObserved("alice"),
        catalog
            .parse(
                Ircv3InboundCommandOperation.WHOIS_END,
                new Ircv3InboundCommandRequest(
                    "server",
                    "318",
                    ":server 318 me alice :End of /WHOIS list",
                    List.of("me", "alice", ":End of /WHOIS list"),
                    Map.of()))
            .getFirst());

    List<Ircv3InboundCommandSignal> whoxSignals =
        catalog.parse(
            Ircv3InboundCommandOperation.WHOX,
            new Ircv3InboundCommandRequest(
                "server",
                "354",
                ":server 354 me 1 #ircafe ident host.example alice G account :more",
                List.of(
                    "me",
                    "1",
                    "#ircafe",
                    "ident",
                    "host.example",
                    "alice",
                    "G",
                    "account",
                    ":more"),
                Map.of()));
    assertEquals(4, whoxSignals.size());
    assertEquals(
        new Ircv3InboundCommandSignal.WhoxSchemaObserved(true, "strict-parse-ok"),
        whoxSignals.get(0));
    assertInstanceOf(Ircv3InboundCommandSignal.ChannelHostmaskObserved.class, whoxSignals.get(1));
    assertEquals(
        new Ircv3InboundCommandSignal.UserAwayObserved("alice", true, null), whoxSignals.get(2));
    assertEquals(
        new Ircv3InboundCommandSignal.AccountObserved(
            "alice", Ircv3InboundCommandSignal.AccountState.LOGGED_IN, "account"),
        whoxSignals.get(3));

    assertEquals(
        new Ircv3InboundCommandSignal.ReadMarkerObserved(
            "#ircafe", "timestamp=2026-01-01T00:00:00Z"),
        catalog
            .parse(
                Ircv3InboundCommandOperation.READ_MARKER,
                new Ircv3InboundCommandRequest(
                    "server",
                    "MARKREAD",
                    ":server MARKREAD #ircafe :timestamp=2026-01-01T00:00:00Z",
                    List.of("#ircafe", ":timestamp=2026-01-01T00:00:00Z"),
                    Map.of()))
            .getFirst());

    assertEquals(
        new Ircv3InboundCommandSignal.MessageRedactionObserved("#ircafe", "msg-42"),
        catalog
            .parse(
                Ircv3InboundCommandOperation.MESSAGE_REDACTION,
                new Ircv3InboundCommandRequest(
                    "alice",
                    "REDACT",
                    ":alice REDACT #ircafe msg-42",
                    List.of("#ircafe", "msg-42"),
                    Map.of()))
            .getFirst());

    assertEquals(
        new Ircv3InboundCommandSignal.HistoryBatchStarted("history-42", "chathistory", "#ircafe"),
        catalog
            .parse(
                Ircv3InboundCommandOperation.HISTORY_BATCH_CONTROL,
                new Ircv3InboundCommandRequest(
                    "server",
                    "BATCH",
                    ":server BATCH +history-42 chathistory #ircafe",
                    List.of("+history-42", "chathistory", "#ircafe"),
                    Map.of()))
            .getFirst());

    assertEquals(
        new Ircv3InboundCommandSignal.ZncDetectedObserved("CAP", "znc.in/playback"),
        catalog
            .parse(
                Ircv3InboundCommandOperation.HISTORY_ZNC_CAPABILITY,
                new Ircv3InboundCommandRequest(
                    "server", "CAP", "", List.of("znc.in/playback"), Map.of()))
            .getFirst());
    assertEquals(
        "RPL_MYINFO/004",
        ((Ircv3InboundCommandSignal.ZncDetectedObserved)
                catalog
                    .parse(
                        Ircv3InboundCommandOperation.HISTORY_ZNC_RPL004,
                        new Ircv3InboundCommandRequest(
                            "server",
                            "004",
                            ":server 004 me irc.example ZNC-1.9.1 oiwsz biklmnopst",
                            List.of(),
                            Map.of()))
                    .getFirst())
            .source());

    List<Ircv3InboundCommandSignal> capabilitySignals =
        catalog.parse(
            Ircv3InboundCommandOperation.CAP_NEGOTIATION,
            new Ircv3InboundCommandRequest(
                "server",
                "CAP",
                ":server CAP me LS :message-tags batch draft/chathistory",
                List.of("me", "LS", ":message-tags", "batch", "draft/chathistory"),
                Map.of(),
                "",
                false,
                0L,
                false,
                false,
                false,
                Set.of()));
    assertEquals(4, capabilitySignals.size());
    assertEquals(
        new Ircv3InboundCommandSignal.CapabilityChangeObserved("LS", "message-tags", false, false),
        capabilitySignals.getFirst());
    assertEquals(
        new Ircv3InboundCommandSignal.CapabilityFallbackPlanned(true, true, "draft/chathistory"),
        capabilitySignals.getLast());

    String isupport = ":server 005 me MONITOR=250 WHOX CLIENTTAGDENY=*,-typing :are supported";
    List<Ircv3InboundCommandSignal> tokenSignals =
        catalog.parse(
            Ircv3InboundCommandOperation.ISUPPORT_TOKENS,
            new Ircv3InboundCommandRequest("server", "005", isupport, List.of(), Map.of()));
    assertEquals(3, tokenSignals.size());
    assertEquals(
        new Ircv3InboundCommandSignal.IsupportTokenObserved("MONITOR", "250", false),
        tokenSignals.getFirst());
    assertEquals(
        new Ircv3InboundCommandSignal.WhoxSupportObserved(true),
        catalog
            .parse(
                Ircv3InboundCommandOperation.ISUPPORT_WHOX,
                new Ircv3InboundCommandRequest("server", "005", isupport, List.of(), Map.of()))
            .getFirst());
    assertEquals(
        new Ircv3InboundCommandSignal.MonitorSupportObserved(true, 250),
        catalog
            .parse(
                Ircv3InboundCommandOperation.ISUPPORT_MONITOR,
                new Ircv3InboundCommandRequest("server", "005", isupport, List.of(), Map.of()))
            .getFirst());
    assertEquals(
        new Ircv3InboundCommandSignal.ClientTagPolicyObserved("typing", true, "*,-typing"),
        catalog
            .parse(
                Ircv3InboundCommandOperation.ISUPPORT_CLIENT_TAG_POLICY,
                new Ircv3InboundCommandRequest("server", "005", isupport, List.of(), Map.of()))
            .getFirst());

    assertEquals(
        new Ircv3InboundCommandSignal.StsPolicyObserved(
            Ircv3InboundCommandSignal.StsPolicyOutcome.LEARN,
            "irc.example.net",
            "duration=60,port=6697,preload",
            61_000L,
            6697,
            true,
            60L),
        catalog
            .parse(
                Ircv3InboundCommandOperation.STS_CAPABILITY,
                new Ircv3InboundCommandRequest(
                    "server",
                    "CAP",
                    "message-tags sts=duration=60,port=6697,preload",
                    List.of(),
                    Map.of(),
                    "IRC.Example.NET",
                    true,
                    1_000L))
            .getFirst());

    assertEquals(
        new Ircv3InboundCommandSignal.SaslCapabilityObserved(
            Ircv3InboundCommandSignal.SaslCapabilityPhase.LIST,
            false,
            true,
            List.of("PLAIN", "SCRAM-SHA-256")),
        catalog
            .parse(
                Ircv3InboundCommandOperation.SASL_CAPABILITY_LIST,
                new Ircv3InboundCommandRequest(
                    "server",
                    "CAP",
                    "",
                    List.of("message-tags", "sasl=plain,SCRAM-SHA-256"),
                    Map.of()))
            .getFirst());
    assertEquals(
        new Ircv3InboundCommandSignal.SaslServerMessageObserved("AUTHENTICATE", "+", null),
        catalog
            .parse(
                Ircv3InboundCommandOperation.SASL_SERVER_MESSAGE,
                new Ircv3InboundCommandRequest(
                    "server", "AUTHENTICATE", "AUTHENTICATE +", List.of("+"), Map.of()))
            .getFirst());
    assertEquals(
        new Ircv3InboundCommandSignal.SaslFailureObserved(
            905,
            "SASL message too long",
            "SASL authentication failed (payload too long): SASL message too long",
            "Login failed — SASL authentication failed (payload too long): SASL message too long"),
        catalog
            .parse(
                Ircv3InboundCommandOperation.SASL_FAILURE,
                new Ircv3InboundCommandRequest(
                    "server",
                    "905",
                    ":server 905 me :SASL message too long",
                    List.of("me", ":SASL message too long"),
                    Map.of()))
            .getFirst());
  }

  @Test
  void builtInMultilineProviderReusesOfferedLimitsOnAck() {
    Ircv3InboundCommandSignalRuntimeCatalog catalog =
        Ircv3InboundCommandSignalRuntimeCatalog.applicationClasspath();

    List<Ircv3InboundCommandSignal> signals =
        catalog.parse(
            Ircv3InboundCommandOperation.MULTILINE_CAPABILITY_STATE,
            Ircv3InboundCommandRequest.multilineCapabilityState(
                "ACK",
                "multiline draft/multiline",
                new Ircv3InboundCommandRequest.MultilineState(
                    4096L, 5L, 0L, 0L, 2048L, 3L, 0L, 0L)));

    assertEquals(
        List.of(
            new Ircv3InboundCommandSignal.MultilineLimitsObserved(false, 4096L, 5L, 4096L, 5L),
            new Ircv3InboundCommandSignal.MultilineLimitsObserved(true, 2048L, 3L, 2048L, 3L)),
        signals);
  }

  @Test
  void higherPriorityProviderReplacesBuiltInOperation() {
    Ircv3InboundCommandSignalRuntimeCatalog catalog =
        Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(
            List.of(provider("built-in", 0, "built-in"), provider("plugin", 100, "plugin")));

    Ircv3InboundCommandSignal.SetNameObserved signal =
        (Ircv3InboundCommandSignal.SetNameObserved)
            catalog.parse(Ircv3InboundCommandOperation.SETNAME, request()).getFirst();
    assertEquals("plugin", signal.realName());
    assertEquals(List.of("plugin"), catalog.providerIds());
  }

  @Test
  void equalPriorityOperationConflictsAreRejected() {
    assertThrows(
        IllegalStateException.class,
        () ->
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(
                List.of(provider("one", 10, "one"), provider("two", 10, "two"))));
  }

  @Test
  void installedProviderConflictIsReportedAndBuiltInsRemainAvailable() {
    RecordingInstalledPlugins plugins =
        new RecordingInstalledPlugins(provider("conflict", 0, "conflict"));

    Ircv3InboundCommandSignalRuntimeCatalog catalog =
        Ircv3InboundCommandSignalRuntimeCatalog.fromInstalledServices(plugins);

    assertTrue(catalog.supports(Ircv3InboundCommandOperation.SETNAME));
    assertEquals(1, plugins.problems.size());
    assertTrue(plugins.problems.getFirst().summary().contains("inbound command-signal"));
  }

  private static Ircv3InboundCommandRequest request() {
    return new Ircv3InboundCommandRequest(
        "alice", "SETNAME", ":alice SETNAME :Alice", List.of(":Alice"), Map.of());
  }

  private static Ircv3InboundCommandSignalProvider provider(
      String providerId, int priority, String response) {
    return new Ircv3InboundCommandSignalProvider() {
      @Override
      public String providerId() {
        return providerId;
      }

      @Override
      public int inboundCommandPriority() {
        return priority;
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
                response,
                Ircv3InboundCommandSignal.SetNameSource.SETNAME));
      }
    };
  }

  private static final class RecordingInstalledPlugins implements InstalledPluginsPort {
    private final Ircv3InboundCommandSignalProvider provider;
    private final List<InstalledPluginProblem> problems = new ArrayList<>();

    private RecordingInstalledPlugins(Ircv3InboundCommandSignalProvider provider) {
      this.provider = provider;
    }

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services = new ArrayList<>(builtInServices);
      if (serviceType.isInstance(provider)) {
        services.add(serviceType.cast(provider));
      }
      return List.copyOf(services);
    }

    @Override
    public void recordPluginProblem(InstalledPluginProblem problem) {
      problems.add(problem);
    }
  }
}
