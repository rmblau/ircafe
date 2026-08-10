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
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3MessageMutationRuntimeSupportTest {

  @Test
  void rendersAllBuiltInOperationsThroughValidatedRuntimeBoundary() {
    Ircv3MessageMutationRuntimeSupport support =
        Ircv3RuntimeTestFixtures.runtime().messageMutation();

    assertEquals(
        "@+reply=abc\\s123\\:xyz\\\\tail PRIVMSG #ircafe :hello",
        support.renderReply("#ircafe", "abc 123;xyz\\tail", "hello").orElseThrow().rawLine());
    assertEquals(
        "@+draft/react=:+1:;+reply=m-1 TAGMSG #ircafe",
        support.renderReaction("#ircafe", "m-1", ":+1:", false).orElseThrow().rawLine());
    assertEquals(
        "@+draft/unreact=:+1:;+reply=m-1 TAGMSG #ircafe",
        support.renderReaction("#ircafe", "m-1", ":+1:", true).orElseThrow().rawLine());
    assertEquals(
        "@+draft/edit=m-1 PRIVMSG #ircafe :fixed text",
        support.renderEdit("#ircafe", "m-1", "fixed text").orElseThrow().rawLine());
    assertEquals(
        "REDACT #ircafe m-1 :cleanup",
        support.renderRedaction("#ircafe", "m-1", "cleanup").orElseThrow().rawLine());
  }

  @Test
  void acceptsReplacementProviderOnlyWhenRequestedSemanticsArePreserved() {
    Ircv3MessageMutationRuntimeSupport support =
        outboundSupport(
            operation ->
                switch (operation) {
                  case REPLY -> "@draft/reply=m-1 PRIVMSG #ircafe :hello";
                  case REACT -> "@draft/react=sparkle;draft/reply=m-1 TAGMSG #ircafe";
                  case UNREACT -> "@draft/unreact=sparkle;reply=m-1 TAGMSG #ircafe";
                  case EDIT -> "@draft/edit=m-1 PRIVMSG #ircafe :fixed";
                  case REDACT -> "redact #ircafe m-1 :cleanup";
                });

    assertEquals(
        "@draft/reply=m-1 PRIVMSG #ircafe :hello",
        support.renderReply("#ircafe", "m-1", "hello").orElseThrow().rawLine());
    assertEquals(
        "@draft/react=sparkle;draft/reply=m-1 TAGMSG #ircafe",
        support.renderReaction("#ircafe", "m-1", "sparkle", false).orElseThrow().rawLine());
    assertEquals(
        "@draft/edit=m-1 PRIVMSG #ircafe :fixed",
        support.renderEdit("#ircafe", "m-1", "fixed").orElseThrow().rawLine());
    assertEquals(
        "redact #ircafe m-1 :cleanup",
        support.renderRedaction("#ircafe", "m-1", "cleanup").orElseThrow().rawLine());
  }

  @Test
  void rejectsUnsafeOrSemanticallyChangedOutboundProviderOutput() {
    assertThrows(
        IllegalStateException.class,
        () ->
            outboundSupport(operation -> "@+reply=m-1 PRIVMSG #other :hello")
                .renderReply("#ircafe", "m-1", "hello"));
    assertThrows(
        IllegalStateException.class,
        () ->
            outboundSupport(operation -> "@+reply=other PRIVMSG #ircafe :hello")
                .renderReply("#ircafe", "m-1", "hello"));
    assertThrows(
        IllegalStateException.class,
        () ->
            outboundSupport(operation -> "@+draft/edit=m-1 PRIVMSG #ircafe :changed")
                .renderEdit("#ircafe", "m-1", "fixed"));
    assertThrows(
        IllegalStateException.class,
        () ->
            outboundSupport(
                    operation -> "@+draft/react=sparkle;+reply=m-1 TAGMSG #ircafe\r\nOPER root")
                .renderReaction("#ircafe", "m-1", "sparkle", false));
    assertThrows(
        IllegalStateException.class,
        () ->
            outboundSupport(operation -> "REDACT #other m-1 :cleanup")
                .renderRedaction("#ircafe", "m-1", "cleanup"));
  }

  @Test
  void validatesInboundReplyReactionEditAndRedactionSignals() {
    Ircv3InboundTagSignalProvider provider =
        tagProvider(
            Map.of(
                Ircv3InboundTagOperation.REPLY,
                List.of(Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.REPLY, "reply-1")),
                Ircv3InboundTagOperation.REACTIONS,
                List.of(
                    new Ircv3InboundTagSignal(
                        Ircv3InboundTagSignalType.REACT, "sparkle", "message-1")),
                Ircv3InboundTagOperation.MESSAGE_EDIT,
                List.of(Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.MESSAGE_EDIT, "edit-1")),
                Ircv3InboundTagOperation.MESSAGE_REDACTION,
                List.of(
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.MESSAGE_REDACTION, "redact-1"))));
    Ircv3MessageMutationRuntimeSupport support = inboundSupport(provider, null);
    Ircv3InboundTagRequest request = request();

    assertEquals("reply-1", support.replyFromTags(request).orElseThrow().messageId());
    assertEquals(
        new Ircv3MessageMutationRuntimeSupport.ReactionObservation(
            Ircv3MessageMutationRuntimeSupport.ReactionOperation.REACT, "sparkle", "message-1"),
        support.reactionFromTags(request).orElseThrow());
    assertEquals("edit-1", support.messageEditFromTags(request).orElseThrow().messageId());
    assertEquals("redact-1", support.redactionFromTags(request).orElseThrow().messageId());
    assertTrue(support.hasNonReplyMutationTag(request));
    assertEquals(3, support.conversationSignals(request).size());
  }

  @Test
  void rejectsAmbiguousOrUnsafeInboundMutationSignals() {
    Ircv3InboundTagSignalProvider provider =
        tagProvider(
            Map.of(
                Ircv3InboundTagOperation.REPLY,
                List.of(
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.REPLY, "one"),
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.REPLY, "two")),
                Ircv3InboundTagOperation.REACTIONS,
                List.of(
                    new Ircv3InboundTagSignal(
                        Ircv3InboundTagSignalType.REACT, "sparkle", "message-1"),
                    new Ircv3InboundTagSignal(
                        Ircv3InboundTagSignalType.UNREACT, "sparkle", "message-1")),
                Ircv3InboundTagOperation.MESSAGE_EDIT,
                List.of(
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.MESSAGE_EDIT, "bad\nedit")),
                Ircv3InboundTagOperation.MESSAGE_REDACTION,
                List.of(
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.MESSAGE_REDACTION, ""))));
    Ircv3MessageMutationRuntimeSupport support = inboundSupport(provider, null);

    assertTrue(support.replyFromTags(request()).isEmpty());
    assertEquals(
        Ircv3MessageMutationRuntimeSupport.ReactionSelectionType.AMBIGUOUS,
        support.reactionSelectionFromTags(request()).type());
    assertTrue(support.messageEditFromTags(request()).isEmpty());
    assertTrue(support.redactionFromTags(request()).isEmpty());
    assertFalse(support.hasNonReplyMutationTag(request()));
    assertTrue(support.conversationSignals(request()).isEmpty());
  }

  @Test
  void validatesDirectRedactionCommandProviderOutput() {
    Ircv3InboundCommandSignalProvider valid =
        commandProvider(
            List.of(new Ircv3InboundCommandSignal.MessageRedactionObserved("#ircafe", "m-1")));
    Ircv3MessageMutationRuntimeSupport support = inboundSupport(null, valid);

    assertEquals(
        new Ircv3MessageMutationRuntimeSupport.CommandRedactionObservation("#ircafe", "m-1"),
        support
            .redactionFromCommand(
                new Ircv3InboundCommandRequest(
                    "alice", "REDACT", "REDACT #ircafe m-1", List.of("#ircafe", "m-1"), Map.of()))
            .orElseThrow());

    Ircv3InboundCommandSignalProvider ambiguous =
        commandProvider(
            List.of(
                new Ircv3InboundCommandSignal.MessageRedactionObserved("#ircafe", "m-1"),
                new Ircv3InboundCommandSignal.MessageRedactionObserved("#ircafe", "m-2")));
    assertTrue(inboundSupport(null, ambiguous).redactionFromCommand(commandRequest()).isEmpty());

    Ircv3InboundCommandSignalProvider unsafe =
        commandProvider(
            List.of(new Ircv3InboundCommandSignal.MessageRedactionObserved("#ircafe", "bad id")));
    assertTrue(inboundSupport(null, unsafe).redactionFromCommand(commandRequest()).isEmpty());
  }

  private static Ircv3MessageMutationRuntimeSupport outboundSupport(Response response) {
    Ircv3MessageMutationProvider provider =
        new Ircv3MessageMutationProvider() {
          @Override
          public String providerId() {
            return "test-mutations";
          }

          @Override
          public Set<Ircv3MessageMutationOperation> operations() {
            return Set.of(Ircv3MessageMutationOperation.values());
          }

          @Override
          public String build(
              Ircv3MessageMutationOperation operation, Ircv3MessageMutationRequest request) {
            return response.forOperation(operation);
          }
        };
    return new Ircv3MessageMutationRuntimeSupport(
        Ircv3MessageMutationRuntimeCatalog.fromProviders(List.of(provider)),
        Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of()),
        Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of()));
  }

  private static Ircv3MessageMutationRuntimeSupport inboundSupport(
      Ircv3InboundTagSignalProvider tagProvider,
      Ircv3InboundCommandSignalProvider commandProvider) {
    return new Ircv3MessageMutationRuntimeSupport(
        Ircv3MessageMutationRuntimeCatalog.fromProviders(List.of()),
        Ircv3InboundTagSignalRuntimeCatalog.fromProviders(
            tagProvider == null ? List.of() : List.of(tagProvider)),
        Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(
            commandProvider == null ? List.of() : List.of(commandProvider)));
  }

  private static Ircv3InboundTagSignalProvider tagProvider(
      Map<Ircv3InboundTagOperation, List<Ircv3InboundTagSignal>> signals) {
    return new Ircv3InboundTagSignalProvider() {
      @Override
      public String providerId() {
        return "test-tags";
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
    };
  }

  private static Ircv3InboundCommandSignalProvider commandProvider(
      List<Ircv3InboundCommandSignal> signals) {
    return new Ircv3InboundCommandSignalProvider() {
      @Override
      public String providerId() {
        return "test-commands";
      }

      @Override
      public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
        return Set.of(Ircv3InboundCommandOperation.MESSAGE_REDACTION);
      }

      @Override
      public List<Ircv3InboundCommandSignal> parse(
          Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
        return signals;
      }
    };
  }

  private static Ircv3InboundTagRequest request() {
    return new Ircv3InboundTagRequest(
        "TAGMSG", "alice", "#ircafe", List.of("#ircafe"), Map.of("plugin", "true"));
  }

  private static Ircv3InboundCommandRequest commandRequest() {
    return new Ircv3InboundCommandRequest(
        "alice", "REDACT", "REDACT #ircafe m-1", List.of("#ircafe", "m-1"), Map.of());
  }

  @FunctionalInterface
  private interface Response {
    String forOperation(Ircv3MessageMutationOperation operation);
  }
}
