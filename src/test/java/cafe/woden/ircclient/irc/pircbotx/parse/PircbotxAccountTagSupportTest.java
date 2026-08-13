package cafe.woden.ircclient.irc.pircbotx.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.IrcEvent;
import cafe.woden.ircclient.irc.ServerIrcEvent;
import cafe.woden.ircclient.irc.ircv3.Ircv3AccountTagRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3InboundTagSignalRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class PircbotxAccountTagSupportTest {

  @Test
  void emitsAccountStateChangesButSuppressesDuplicates() {
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxAccountTagSupport support = support("libera", out::add);
    Instant now = Instant.parse("2026-03-23T12:00:00Z");

    support.observe(now, "alice", "PRIVMSG", "#ircafe", ImmutableMap.of("account", "alice"));
    support.observe(
        now.plusSeconds(1), "alice", "NOTICE", "#ircafe", ImmutableMap.of("account", "alice"));
    support.observe(
        now.plusSeconds(2), "alice", "PRIVMSG", "#ircafe", ImmutableMap.of("account", "*"));
    support.observe(
        now.plusSeconds(3), "alice", "PRIVMSG", "#ircafe", ImmutableMap.of("account", "0"));

    List<IrcEvent.UserAccountStateObserved> events =
        out.stream()
            .map(ServerIrcEvent::event)
            .filter(IrcEvent.UserAccountStateObserved.class::isInstance)
            .map(IrcEvent.UserAccountStateObserved.class::cast)
            .toList();

    assertEquals(3, events.size());
    assertEquals(IrcEvent.AccountState.LOGGED_IN, events.get(0).accountState());
    assertEquals("alice", events.get(0).accountName());
    assertEquals(IrcEvent.AccountState.LOGGED_OUT, events.get(1).accountState());
    assertNull(events.get(1).accountName());
    assertEquals(IrcEvent.AccountState.LOGGED_OUT, events.get(2).accountState());
    assertNull(events.get(2).accountName());
  }

  @Test
  void runtimeProviderCanOverrideAccountTagInterpretation() {
    List<ServerIrcEvent> out = new ArrayList<>();
    Ircv3InboundTagSignalProvider provider =
        new Ircv3InboundTagSignalProvider() {
          @Override
          public String providerId() {
            return "account-tag-test";
          }

          @Override
          public Set<Ircv3InboundTagOperation> inboundTagOperations() {
            return Set.of(Ircv3InboundTagOperation.ACCOUNT_TAG);
          }

          @Override
          public List<Ircv3InboundTagSignal> parse(
              Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
            return List.of(
                new Ircv3InboundTagSignal(
                    Ircv3InboundTagSignalType.ACCOUNT_TAG, request.sourceNick(), "plugin-account"));
          }
        };
    PircbotxAccountTagSupport support =
        support(
            "libera",
            out::add,
            Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of(provider)));

    support.observe(
        Instant.parse("2026-03-23T12:03:00Z"),
        "alice",
        "PRIVMSG",
        "#ircafe",
        ImmutableMap.of("account", "ignored"));

    IrcEvent.UserAccountStateObserved event =
        (IrcEvent.UserAccountStateObserved) out.getFirst().event();
    assertEquals("plugin-account", event.accountName());
  }

  @Test
  void rejectsRuntimeProvidersThatChangeTheObservedNick() {
    List<ServerIrcEvent> out = new ArrayList<>();
    Ircv3InboundTagSignalProvider provider =
        new Ircv3InboundTagSignalProvider() {
          @Override
          public String providerId() {
            return "account-tag-reroute-test";
          }

          @Override
          public Set<Ircv3InboundTagOperation> inboundTagOperations() {
            return Set.of(Ircv3InboundTagOperation.ACCOUNT_TAG);
          }

          @Override
          public List<Ircv3InboundTagSignal> parse(
              Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
            return List.of(
                new Ircv3InboundTagSignal(
                    Ircv3InboundTagSignalType.ACCOUNT_TAG, "mallory", "plugin-account"));
          }
        };
    PircbotxAccountTagSupport support =
        support(
            "libera",
            out::add,
            Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of(provider)));

    support.observe(
        Instant.parse("2026-03-23T12:04:00Z"),
        "alice",
        "PRIVMSG",
        "#ircafe",
        ImmutableMap.of("account", "wire-account"));

    assertTrue(out.isEmpty());
  }

  @Test
  void ignoresMissingAccountTag() {
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxAccountTagSupport support = support("libera", out::add);

    support.observe(
        Instant.parse("2026-03-23T12:05:00Z"),
        "alice",
        "PRIVMSG",
        "#ircafe",
        ImmutableMap.of("msgid", "123"));

    assertTrue(out.isEmpty());
  }

  private static PircbotxAccountTagSupport support(String serverId, Consumer<ServerIrcEvent> sink) {
    return PircbotxParserRuntimeTestFixtures.accountTags(serverId, sink);
  }

  private static PircbotxAccountTagSupport support(
      String serverId,
      Consumer<ServerIrcEvent> sink,
      Ircv3InboundTagSignalRuntimeCatalog inboundTagCatalog) {
    return new PircbotxAccountTagSupport(
        serverId, sink, new Ircv3AccountTagRuntimeSupport(inboundTagCatalog));
  }
}
