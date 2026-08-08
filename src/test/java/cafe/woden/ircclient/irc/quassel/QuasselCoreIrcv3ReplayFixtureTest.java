package cafe.woden.ircclient.irc.quassel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.IrcPropertiesTestFixtures;
import cafe.woden.ircclient.config.servers.ServerCatalog;
import cafe.woden.ircclient.irc.*;
import cafe.woden.ircclient.irc.backend.*;
import cafe.woden.ircclient.util.RxVirtualSchedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class QuasselCoreIrcv3ReplayFixtureTest {

  @AfterEach
  void tearDownSchedulers() {
    RxVirtualSchedulers.shutdown();
  }

  @Test
  void replayFixtureFramesEmitExpectedIrcv3SignalEvents() throws Exception {
    ServerCatalog serverCatalog = mock(ServerCatalog.class);
    QuasselCoreSocketConnector connector = mock(QuasselCoreSocketConnector.class);
    QuasselCoreProtocolProbe protocolProbe = mock(QuasselCoreProtocolProbe.class);
    QuasselCoreAuthHandshake authHandshake = mock(QuasselCoreAuthHandshake.class);
    QuasselCoreDatastreamCodec datastreamCodec = new QuasselCoreDatastreamCodec();
    IrcProperties.Server server = server();
    BlockingSocket socket = new BlockingSocket();
    QuasselCoreProtocolProbe.ProbeSelection probeSelection =
        new QuasselCoreProtocolProbe.ProbeSelection(
            0x00000002, QuasselCoreProtocolProbe.PROTOCOL_DATASTREAM, 0, 0);
    QuasselCoreDatastreamCodec.BufferInfoValue chan =
        new QuasselCoreDatastreamCodec.BufferInfoValue(11, 1, 0x02, -1, "#ircafe");

    when(serverCatalog.require("quassel")).thenReturn(server);
    when(connector.connect(server)).thenReturn(socket);
    when(protocolProbe.negotiate(socket)).thenReturn(probeSelection);
    when(authHandshake.authenticate(socket, server))
        .thenReturn(
            new QuasselCoreAuthHandshake.AuthResult("quassel", 1, List.of(1), Map.of(11, chan)));

    QuasselCoreIrcClientService service =
        QuasselRuntimeTestFixtures.service(
            serverCatalog, connector, protocolProbe, authHandshake, datastreamCodec);
    TestSubscriber<ServerIrcEvent> events = service.events().test();
    service.connect("quassel").blockingAwait();
    events.awaitCount(3);

    List<ReplayMessage> replayMessages = loadReplayFixture("/quassel/replay/ircv3-inbound.tsv");
    long now = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    long seq = 700L;
    for (ReplayMessage replay : replayMessages) {
      QuasselCoreDatastreamCodec.MessageValue msg =
          new QuasselCoreDatastreamCodec.MessageValue(
              seq++, now++, replay.typeBits(), 0, chan, replay.sender(), replay.content());
      socket.writeInbound(encodeRpcCall(datastreamCodec, "2displayMsg(Message)", List.of(msg)));
    }

    awaitFixtureEvents(events);

    assertTrue(
        events.values().stream()
            .map(ServerIrcEvent::event)
            .anyMatch(IrcEvent.MessageReplyObserved.class::isInstance));
    assertTrue(
        events.values().stream()
            .map(ServerIrcEvent::event)
            .anyMatch(IrcEvent.MessageReactObserved.class::isInstance));
    assertTrue(
        events.values().stream()
            .map(ServerIrcEvent::event)
            .anyMatch(IrcEvent.UserTypingObserved.class::isInstance));
    assertTrue(
        events.values().stream()
            .map(ServerIrcEvent::event)
            .anyMatch(IrcEvent.ReadMarkerObserved.class::isInstance));
    assertTrue(
        events.values().stream()
            .map(ServerIrcEvent::event)
            .anyMatch(IrcEvent.MessageRedactionObserved.class::isInstance));
    assertTrue(
        events.values().stream()
            .map(ServerIrcEvent::event)
            .anyMatch(
                ev ->
                    ev instanceof IrcEvent.ChannelMessage msg
                        && "msg-1".equals(msg.ircv3Tags().get("draft/reply"))));
    assertFalse(
        events.values().stream()
            .map(ServerIrcEvent::event)
            .anyMatch(
                ev ->
                    ev instanceof IrcEvent.ChannelMessage msg
                        && msg.text().contains("TAGMSG #ircafe")));
  }

  private static List<ReplayMessage> loadReplayFixture(String resourcePath) throws IOException {
    InputStream in = QuasselCoreIrcv3ReplayFixtureTest.class.getResourceAsStream(resourcePath);
    if (in == null) {
      throw new IOException("missing replay fixture: " + resourcePath);
    }

    ArrayList<ReplayMessage> out = new ArrayList<>();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String raw = line.trim();
        if (raw.isEmpty() || raw.startsWith("#")) continue;
        String[] parts = raw.split("\t", 3);
        if (parts.length < 3) {
          throw new IOException("invalid replay fixture row: " + raw);
        }
        int typeBits;
        try {
          typeBits = Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
          throw new IOException("invalid type bits in replay fixture row: " + raw, e);
        }
        String sender = parts[1].trim();
        String content = parts[2];
        out.add(new ReplayMessage(typeBits, sender, content));
      }
    }
    if (out.isEmpty()) {
      throw new IOException("replay fixture is empty: " + resourcePath);
    }
    return List.copyOf(out);
  }

  private static void awaitFixtureEvents(TestSubscriber<ServerIrcEvent> events)
      throws InterruptedException {
    awaitCondition(
        () ->
            containsEvent(events, IrcEvent.MessageReplyObserved.class::isInstance)
                && containsEvent(events, IrcEvent.MessageReactObserved.class::isInstance)
                && containsEvent(events, IrcEvent.UserTypingObserved.class::isInstance)
                && containsEvent(events, IrcEvent.ReadMarkerObserved.class::isInstance)
                && containsEvent(events, IrcEvent.MessageRedactionObserved.class::isInstance)
                && containsEvent(
                    events,
                    ev ->
                        ev instanceof IrcEvent.ChannelMessage msg
                            && "msg-1".equals(msg.ircv3Tags().get("draft/reply"))));
  }

  private static boolean containsEvent(
      TestSubscriber<ServerIrcEvent> events, Predicate<IrcEvent> predicate) {
    return events.values().stream().map(ServerIrcEvent::event).anyMatch(predicate);
  }

  private static void awaitCondition(BooleanSupplier condition) throws InterruptedException {
    long deadline = System.currentTimeMillis() + 2_000L;
    while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
      Thread.sleep(10L);
    }
    assertTrue(condition.getAsBoolean());
  }

  private static byte[] encodeRpcCall(
      QuasselCoreDatastreamCodec codec, String slotName, List<Object> params) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    codec.writeSignalProxyRpcCall(out, slotName, params);
    return out.toByteArray();
  }

  private static IrcProperties.Server server() {
    return IrcPropertiesTestFixtures.serverBuilder("quassel")
        .port(4242)
        .tls(false)
        .nick("quassel")
        .login("quassel")
        .realName("Quassel Test")
        .backend(IrcProperties.Server.Backend.QUASSEL_CORE)
        .build();
  }

  private record ReplayMessage(int typeBits, String sender, String content) {}

  private static final class BlockingSocket extends Socket {
    private final java.io.PipedInputStream input;
    private final java.io.PipedOutputStream inputWriter;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private volatile boolean closed;

    private BlockingSocket() throws IOException {
      this.input = new java.io.PipedInputStream();
      this.inputWriter = new java.io.PipedOutputStream(input);
    }

    @Override
    public InputStream getInputStream() {
      return input;
    }

    @Override
    public OutputStream getOutputStream() {
      return output;
    }

    @Override
    public synchronized void close() throws IOException {
      if (closed) return;
      closed = true;
      inputWriter.close();
      input.close();
    }

    @Override
    public boolean isClosed() {
      return closed;
    }

    private void writeInbound(byte[] frame) throws IOException {
      inputWriter.write(frame);
      inputWriter.flush();
    }
  }
}
