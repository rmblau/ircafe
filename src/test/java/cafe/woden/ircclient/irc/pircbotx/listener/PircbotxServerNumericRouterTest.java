package cafe.woden.ircclient.irc.pircbotx.listener;

import static cafe.woden.ircclient.irc.pircbotx.PircbotxRuntimeTestFixtures.monitorEvents;
import static cafe.woden.ircclient.irc.pircbotx.PircbotxRuntimeTestFixtures.runtime;
import static cafe.woden.ircclient.irc.pircbotx.PircbotxRuntimeTestFixtures.serverResponses;
import static cafe.woden.ircclient.irc.pircbotx.PircbotxRuntimeTestFixtures.whoEvents;
import static cafe.woden.ircclient.irc.pircbotx.listener.PircbotxListenerRuntimeTestFixtures.isupportObserver;
import static cafe.woden.ircclient.irc.pircbotx.listener.PircbotxListenerRuntimeTestFixtures.registrationLifecycle;
import static cafe.woden.ircclient.irc.pircbotx.listener.PircbotxListenerRuntimeTestFixtures.saslFailures;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.bouncer.BouncerBackendRegistry;
import cafe.woden.ircclient.bouncer.BouncerDiscoveryEventPort;
import cafe.woden.ircclient.irc.*;
import cafe.woden.ircclient.irc.backend.*;
import cafe.woden.ircclient.irc.ircv3.*;
import cafe.woden.ircclient.irc.pircbotx.emit.PircbotxMonitorEventEmitter;
import cafe.woden.ircclient.irc.pircbotx.emit.PircbotxServerResponseEmitter;
import cafe.woden.ircclient.irc.pircbotx.emit.PircbotxWhoEventEmitter;
import cafe.woden.ircclient.irc.pircbotx.parse.PircbotxPresenceSignalSupport;
import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import cafe.woden.ircclient.irc.playback.*;
import cafe.woden.ircclient.state.ServerIsupportState;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.ServerResponseEvent;

class PircbotxServerNumericRouterTest {

  @Test
  void onServerResponseEmitsFeaturesAndStatusLineForIsupport() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");
    AtomicReference<String> selfHint = new AtomicReference<>("");
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxServerNumericRouter router = newRouter(conn, selfHint::set, events);

    ServerResponseEvent event =
        serverResponse(5, ":server 005 me CHANTYPES=# MONITOR=100 :are supported by this server");

    router.onServerResponse(event);

    assertEquals("me", selfHint.get());
    assertEquals(2, events.size());
    IrcEvent.ConnectionFeaturesUpdated updated =
        assertInstanceOf(IrcEvent.ConnectionFeaturesUpdated.class, events.get(0).event());
    IrcEvent.ServerResponseLine line =
        assertInstanceOf(IrcEvent.ServerResponseLine.class, events.get(1).event());
    assertEquals("isupport", updated.source());
    assertEquals(5, line.code());
  }

  @Test
  void onServerResponseEmitsJoinFailedForJoinFailureNumeric() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxServerNumericRouter router = newRouter(conn, ignored -> {}, events);

    router.onServerResponse(
        serverResponse(473, ":server 473 me #locked :Cannot join channel (+i)"));

    assertEquals(1, events.size());
    IrcEvent.JoinFailed failed =
        assertInstanceOf(IrcEvent.JoinFailed.class, events.getFirst().event());
    assertEquals("#locked", failed.channel());
    assertEquals(473, failed.code());
  }

  @Test
  void onServerResponseEmitsRedirectAndStatusLineForErrLinkchannel() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxServerNumericRouter router = newRouter(conn, ignored -> {}, events);

    router.onServerResponse(
        serverResponse(470, ":server 470 me #old #new :Forwarding to another channel"));

    assertEquals(2, events.size());
    IrcEvent.ChannelRedirected redirected =
        assertInstanceOf(IrcEvent.ChannelRedirected.class, events.get(0).event());
    IrcEvent.ServerResponseLine line =
        assertInstanceOf(IrcEvent.ServerResponseLine.class, events.get(1).event());
    assertEquals("#old", redirected.fromChannel());
    assertEquals("#new", redirected.toChannel());
    assertEquals(470, line.code());
  }

  @Test
  void onServerResponseEmitsAwayStatusChangedFor306() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxServerNumericRouter router = newRouter(conn, ignored -> {}, events);

    router.onServerResponse(
        serverResponse(306, ":server 306 me :You have been marked as being away"));

    assertEquals(1, events.size());
    IrcEvent.AwayStatusChanged away =
        assertInstanceOf(IrcEvent.AwayStatusChanged.class, events.getFirst().event());
    assertEquals(true, away.away());
  }

  private static PircbotxServerNumericRouter newRouter(
      PircbotxConnectionState conn,
      java.util.function.Consumer<String> selfHint,
      List<ServerIrcEvent> events) {
    var testRuntime = runtime();
    PircbotxBouncerDiscoveryCoordinator bouncerDiscovery =
        new PircbotxBouncerDiscoveryCoordinator(
            "libera",
            conn,
            false,
            false,
            new BouncerBackendRegistry(List.of()),
            BouncerDiscoveryEventPort.noOp());
    PircbotxServerResponseEmitter serverResponses =
        serverResponses("libera", events::add, testRuntime);
    PircbotxSaslFailureHandler saslFailures =
        saslFailures("libera", conn, events::add, false, testRuntime);
    PircbotxMonitorEventEmitter monitorEvents = monitorEvents("libera", events::add, testRuntime);
    PircbotxIsupportObserver isupportObserver =
        isupportObserver(
            "libera",
            conn,
            new ServerIsupportState(),
            events::add,
            bouncerDiscovery::observeSojuBouncerNetId,
            testRuntime);
    PircbotxRegistrationLifecycleHandler registrationLifecycle =
        registrationLifecycle(
            "libera",
            conn,
            new NoOpPlaybackCursorProvider(),
            bouncerDiscovery,
            serverResponses,
            events::add,
            testRuntime);
    PircbotxWhoEventEmitter whoEvents = whoEvents("libera", conn, events::add, testRuntime);
    return new PircbotxServerNumericRouter(
        "libera",
        selfHint,
        events::add,
        saslFailures,
        monitorEvents,
        isupportObserver,
        registrationLifecycle,
        whoEvents,
        serverResponses,
        new PircbotxPresenceSignalSupport(
            "libera", events::add, testRuntime.catalogs().inboundCommands()));
  }

  private static ServerResponseEvent serverResponse(int code, String line) {
    ServerResponseEvent event = mock(ServerResponseEvent.class);
    PircBotX bot = mock(PircBotX.class);
    when(event.getCode()).thenReturn(code);
    when(event.getRawLine()).thenReturn(line);
    when(event.getBot()).thenReturn(bot);
    return event;
  }
}
