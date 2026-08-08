package cafe.woden.ircclient.irc.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.irc.ircv3.Ircv3InboundCommandSignalRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3InboundTagSignalRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageMutationRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageTagsRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3OutboundCommandRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3RuntimeCatalogs;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParseRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParseResult;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParserProvider;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MatrixIrcv3RuntimeSupportTest {

  @Test
  void parsesRawTagsThroughSelectedRuntimeProvider() {
    MatrixIrcv3RuntimeSupport support =
        support(
            request -> new Ircv3MessageTagParseResult(Map.of("runtime/reply", "event-42")),
            null);

    assertEquals(
        Map.of("runtime/reply", "event-42"),
        support.messageTags("@ignored=value PRIVMSG #ircafe :hello"));
  }

  @Test
  void bundleConstructorDoesNotReloadApplicationClasspathProviders() {
    Ircv3RuntimeCatalogs catalogs =
        catalogs(
            request ->
                new Ircv3MessageTagParseResult(Map.of("runtime/reply", "event-42")),
            null);
    Thread thread = Thread.currentThread();
    ClassLoader originalClassLoader = thread.getContextClassLoader();
    thread.setContextClassLoader(new RejectingServiceLoaderClassLoader(originalClassLoader));
    try {
      MatrixIrcv3RuntimeSupport support = new MatrixIrcv3RuntimeSupport(catalogs);
      assertEquals(
          Map.of("runtime/reply", "event-42"),
          support.messageTags("@ignored=value PRIVMSG #ircafe :hello"));
    } finally {
      thread.setContextClassLoader(originalClassLoader);
    }
  }

  @Test
  void interpretsReplyEditTypingAndReactionSignalsThroughRuntimeProviders() {
    StubInboundProvider provider =
        new StubInboundProvider(
            Map.of(
                Ircv3InboundTagOperation.REPLY,
                List.of(
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.REPLY, "reply-event")),
                Ircv3InboundTagOperation.MESSAGE_EDIT,
                List.of(
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.MESSAGE_EDIT, "edit-event")),
                Ircv3InboundTagOperation.TYPING,
                List.of(
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.TYPING, "active")),
                Ircv3InboundTagOperation.REACTIONS,
                List.of(
                    new Ircv3InboundTagSignal(
                        Ircv3InboundTagSignalType.REACT, "👍", "reaction-target"))));
    MatrixIrcv3RuntimeSupport support = support(request -> requestResult(request), provider);

    List<String> parameters = List.of("#ircafe", "hello");
    Map<String, String> tags = Map.of("runtime", "true");
    String rawLine = "@runtime=true PRIVMSG #ircafe :hello";

    assertEquals(
        "reply-event",
        support.replyTarget("PRIVMSG", "#ircafe", parameters, tags, rawLine));
    assertEquals(
        "edit-event",
        support.messageEditTarget("PRIVMSG", "#ircafe", parameters, tags, rawLine));
    assertEquals(
        "active",
        support.typingState("TAGMSG", "#ircafe", List.of("#ircafe"), tags, rawLine));
    assertEquals(
        new MatrixIrcv3RuntimeSupport.ReactionPlan(
            MatrixIrcv3RuntimeSupport.ReactionType.REACT, "reaction-target", "👍"),
        support.reaction("TAGMSG", "#ircafe", List.of("#ircafe"), tags, rawLine));
  }

  @Test
  void rejectsAmbiguousReactionProviderOutput() {
    StubInboundProvider provider =
        new StubInboundProvider(
            Map.of(
                Ircv3InboundTagOperation.REACTIONS,
                List.of(
                    new Ircv3InboundTagSignal(
                        Ircv3InboundTagSignalType.REACT, "👍", "event-1"),
                    new Ircv3InboundTagSignal(
                        Ircv3InboundTagSignalType.UNREACT, "👍", "event-1"))));
    MatrixIrcv3RuntimeSupport support = support(request -> requestResult(request), provider);

    assertEquals(
        MatrixIrcv3RuntimeSupport.ReactionPlan.ambiguous(),
        support.reaction("TAGMSG", "#ircafe", List.of("#ircafe"), Map.of(), ""));
  }

  @Test
  void ignoresUnexpectedAndConflictingProviderSignals() {
    StubInboundProvider provider =
        new StubInboundProvider(
            Map.of(
                Ircv3InboundTagOperation.REPLY,
                List.of(
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.REPLY, "event-1"),
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.REPLY, "event-2")),
                Ircv3InboundTagOperation.MESSAGE_EDIT,
                List.of(
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.TYPING, "active")),
                Ircv3InboundTagOperation.TYPING,
                List.of(
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.TYPING, "invalid"))));
    MatrixIrcv3RuntimeSupport support = support(request -> requestResult(request), provider);

    assertEquals("", support.replyTarget("PRIVMSG", "#ircafe", List.of(), Map.of(), ""));
    assertEquals(
        "", support.messageEditTarget("PRIVMSG", "#ircafe", List.of(), Map.of(), ""));
    assertEquals("", support.typingState("TAGMSG", "#ircafe", List.of(), Map.of(), ""));
  }

  private static MatrixIrcv3RuntimeSupport support(
      Parser parser, Ircv3InboundTagSignalProvider inboundProvider) {
    return new MatrixIrcv3RuntimeSupport(catalogs(parser, inboundProvider));
  }

  private static Ircv3RuntimeCatalogs catalogs(
      Parser parser, Ircv3InboundTagSignalProvider inboundProvider) {
    Ircv3MessageTagParserProvider parserProvider =
        new Ircv3MessageTagParserProvider() {
          @Override
          public String providerId() {
            return "matrix-test-tags";
          }

          @Override
          public Ircv3MessageTagParseResult parse(Ircv3MessageTagParseRequest request) {
            return parser.parse(request);
          }
        };
    return catalogs(parserProvider, inboundProvider);
  }

  private static Ircv3RuntimeCatalogs catalogs(
      Ircv3MessageTagParserProvider parserProvider,
      Ircv3InboundTagSignalProvider inboundProvider) {
    return new Ircv3RuntimeCatalogs(
        Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of()),
        Ircv3InboundTagSignalRuntimeCatalog.fromProviders(
            inboundProvider == null ? List.of() : List.of(inboundProvider)),
        Ircv3OutboundCommandRuntimeCatalog.fromProviders(List.of()),
        Ircv3MessageMutationRuntimeCatalog.fromProviders(List.of()),
        Ircv3MessageTagsRuntimeCatalog.fromProviders(List.of(parserProvider)));
  }

  private static Ircv3MessageTagParseResult requestResult(Ircv3MessageTagParseRequest request) {
    return new Ircv3MessageTagParseResult(request == null ? Map.of() : request.transportTags());
  }

  @FunctionalInterface
  private interface Parser {
    Ircv3MessageTagParseResult parse(Ircv3MessageTagParseRequest request);
  }

  private static final class RejectingServiceLoaderClassLoader extends ClassLoader {

    private RejectingServiceLoaderClassLoader(ClassLoader parent) {
      super(parent);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
      if (name != null && name.startsWith("META-INF/services/")) {
        throw new AssertionError("Matrix runtime wiring reloaded ServiceLoader providers: " + name);
      }
      return super.getResources(name);
    }
  }

  private static final class StubInboundProvider implements Ircv3InboundTagSignalProvider {

    private final Map<Ircv3InboundTagOperation, List<Ircv3InboundTagSignal>> signals;

    private StubInboundProvider(
        Map<Ircv3InboundTagOperation, List<Ircv3InboundTagSignal>> signals) {
      this.signals = Map.copyOf(signals);
    }

    @Override
    public String providerId() {
      return "matrix-test-signals";
    }

    @Override
    public Set<Ircv3InboundTagOperation> inboundTagOperations() {
      return signals.keySet();
    }

    @Override
    public List<Ircv3InboundTagSignal> parse(
        Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
      return signals.getOrDefault(operation, List.of());
    }
  }
}
