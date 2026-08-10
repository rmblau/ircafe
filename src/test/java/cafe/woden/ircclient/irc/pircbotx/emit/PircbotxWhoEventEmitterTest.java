package cafe.woden.ircclient.irc.pircbotx.emit;

import static cafe.woden.ircclient.irc.pircbotx.PircbotxRuntimeTestFixtures.whoEvents;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.*;
import cafe.woden.ircclient.irc.backend.*;
import cafe.woden.ircclient.irc.ircv3.*;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import cafe.woden.ircclient.irc.playback.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PircbotxWhoEventEmitterTest {

  @Test
  void maybeEmitNumeric302EmitsHostmaskAndAway() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxWhoEventEmitter emitter = whoEvents("libera", conn, events::add);

    assertTrue(
        emitter.maybeEmitNumeric(
            302, ":server 302 me :alice=+ident@host.example bob*=-user2@host2.example"));

    assertEquals(4, events.size());
    IrcEvent.UserHostmaskObserved aliceHostmask =
        assertInstanceOf(IrcEvent.UserHostmaskObserved.class, events.get(0).event());
    IrcEvent.UserAwayStateObserved aliceAway =
        assertInstanceOf(IrcEvent.UserAwayStateObserved.class, events.get(1).event());
    IrcEvent.UserHostmaskObserved bobHostmask =
        assertInstanceOf(IrcEvent.UserHostmaskObserved.class, events.get(2).event());
    IrcEvent.UserAwayStateObserved bobAway =
        assertInstanceOf(IrcEvent.UserAwayStateObserved.class, events.get(3).event());

    assertEquals("alice", aliceHostmask.nick());
    assertEquals("alice!ident@host.example", aliceHostmask.hostmask());
    assertEquals(IrcEvent.AwayState.HERE, aliceAway.awayState());
    assertEquals("bob", bobHostmask.nick());
    assertEquals("bob!user2@host2.example", bobHostmask.hostmask());
    assertEquals(IrcEvent.AwayState.AWAY, bobAway.awayState());
  }

  @Test
  void maybeEmitNumeric354StrictEmitsSchemaHostmaskAwayAndAccount() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxWhoEventEmitter emitter = whoEvents("libera", conn, events::add);

    assertTrue(
        emitter.maybeEmitNumeric(
            354, ":server 354 me 1 #ircafe ident host.example alice G account :more"));

    assertEquals(4, events.size());
    IrcEvent.WhoxSchemaCompatibleObserved compatible =
        assertInstanceOf(IrcEvent.WhoxSchemaCompatibleObserved.class, events.get(0).event());
    IrcEvent.UserHostmaskObserved hostmask =
        assertInstanceOf(IrcEvent.UserHostmaskObserved.class, events.get(1).event());
    IrcEvent.UserAwayStateObserved away =
        assertInstanceOf(IrcEvent.UserAwayStateObserved.class, events.get(2).event());
    IrcEvent.UserAccountStateObserved account =
        assertInstanceOf(IrcEvent.UserAccountStateObserved.class, events.get(3).event());

    assertTrue(compatible.compatible());
    assertEquals("#ircafe", hostmask.channel());
    assertEquals("alice!ident@host.example", hostmask.hostmask());
    assertEquals(IrcEvent.AwayState.AWAY, away.awayState());
    assertEquals(IrcEvent.AccountState.LOGGED_IN, account.accountState());
    assertEquals("account", account.accountName());
  }

  @Test
  void maybeEmitNumeric318EmitsHereLogoutAndProbeCompleted() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");
    conn.beginWhoisProbe("alice");
    conn.markWhoisAccountObserved("bob");

    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxWhoEventEmitter emitter = whoEvents("libera", conn, events::add);

    assertTrue(emitter.maybeEmitNumeric(318, ":server 318 me alice :End of /WHOIS list"));

    assertEquals(3, events.size());
    IrcEvent.UserAwayStateObserved away =
        assertInstanceOf(IrcEvent.UserAwayStateObserved.class, events.get(0).event());
    IrcEvent.UserAccountStateObserved account =
        assertInstanceOf(IrcEvent.UserAccountStateObserved.class, events.get(1).event());
    IrcEvent.WhoisProbeCompleted completed =
        assertInstanceOf(IrcEvent.WhoisProbeCompleted.class, events.get(2).event());

    assertEquals(IrcEvent.AwayState.HERE, away.awayState());
    assertEquals(IrcEvent.AccountState.LOGGED_OUT, account.accountState());
    assertEquals("alice", completed.nick());
    assertTrue(completed.whoisAccountNumericSupported());
  }

  @Test
  void maybeEmitLineEmitsWhoisUserHostmask() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxWhoEventEmitter emitter = whoEvents("libera", conn, events::add);

    emitter.maybeEmitLine(":server 311 me alice ident host.example * :Alice Example");

    assertEquals(1, events.size());
    IrcEvent.UserHostmaskObserved hostmask =
        assertInstanceOf(IrcEvent.UserHostmaskObserved.class, events.get(0).event());
    assertEquals("alice", hostmask.nick());
    assertEquals("alice!ident@host.example", hostmask.hostmask());
  }

  @Test
  void maybeEmitNumeric352UsesSelectedRuntimeProvider() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");
    List<ServerIrcEvent> events = new ArrayList<>();
    Ircv3InboundCommandSignalProvider plugin =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "plugin-names";
          }

          @Override
          public int inboundCommandPriority() {
            return 100;
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(Ircv3InboundCommandOperation.WHO);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            return List.of(
                new Ircv3InboundCommandSignal.ChannelHostmaskObserved(
                    "#plugin", "override", "override!ident@plugin.example"),
                new Ircv3InboundCommandSignal.UserAwayObserved("override", false, null));
          }
        };
    PircbotxWhoEventEmitter emitter =
        new PircbotxWhoEventEmitter(
            "libera",
            conn,
            events::add,
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(plugin)));

    assertTrue(emitter.maybeEmitNumeric(352, ":server 352 ignored malformed"));

    assertEquals(2, events.size());
    IrcEvent.UserHostmaskObserved hostmask =
        assertInstanceOf(IrcEvent.UserHostmaskObserved.class, events.get(0).event());
    IrcEvent.UserAwayStateObserved away =
        assertInstanceOf(IrcEvent.UserAwayStateObserved.class, events.get(1).event());
    assertEquals("#plugin", hostmask.channel());
    assertEquals("override", hostmask.nick());
    assertEquals("override!ident@plugin.example", hostmask.hostmask());
    assertEquals(IrcEvent.AwayState.HERE, away.awayState());
  }
}
