package cafe.woden.ircclient.app.outbound.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationTargetView;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageMutationRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationRequest;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MessageMutationOutboundCommandsRouterTest {

  @Test
  void routesBuiltInBackendsThroughRuntimeIrcv3Providers() {
    MessageMutationOutboundCommandsRouter router =
        cafe.woden.ircclient.app.outbound.TestBackendSupport
            .builtInMessageMutationOutboundCommandsRouter();

    assertInstanceOf(Ircv3MessageMutationOutboundCommands.class, router.commandsFor("irc"));
    assertInstanceOf(Ircv3MessageMutationOutboundCommands.class, router.commandsFor("matrix"));
    assertInstanceOf(
        Ircv3MessageMutationOutboundCommands.class, router.commandsFor("quassel-core"));
    assertMessageMutationCommands(router.commandsFor("irc"), "irc");
    assertMessageMutationCommands(router.commandsFor("matrix"), "matrix");
    assertMessageMutationCommands(router.commandsFor("quassel-core"), "quassel-core");
  }

  @Test
  void fallsBackToRuntimeIrcHandlerWhenBackendHasNoRegisteredHandler() {
    MessageMutationOutboundCommandsRouter router =
        cafe.woden.ircclient.app.outbound.TestBackendSupport.messageMutationOutboundCommandsRouter(
            List.of(new TestMessageMutationOutboundCommands("matrix")));

    assertMessageMutationCommands(router.commandsFor(""), "irc");
    assertMessageMutationCommands(router.commandsFor("quassel-core"), "quassel-core");
  }

  @Test
  void rejectsRuntimeProviderThatChangesRequestedTarget() {
    Ircv3MessageMutationProvider provider =
        new Ircv3MessageMutationProvider() {
          @Override
          public String providerId() {
            return "unsafe-reply";
          }

          @Override
          public Set<Ircv3MessageMutationOperation> operations() {
            return Set.of(Ircv3MessageMutationOperation.REPLY);
          }

          @Override
          public String build(
              Ircv3MessageMutationOperation operation, Ircv3MessageMutationRequest request) {
            return "@+reply=" + request.messageId() + " PRIVMSG #other :" + request.payload();
          }
        };
    MessageMutationOutboundCommands commands =
        new Ircv3MessageMutationOutboundCommands(
            "irc", Ircv3MessageMutationRuntimeCatalog.fromProviders(List.of(provider)));

    assertThrows(
        IllegalStateException.class,
        () ->
            commands.buildReplyRawLine(
                new MessageMutationTargetView("server", "#ircafe"), "m-1", "hello"));
  }

  @Test
  void rejectsDuplicateBackendHandlers() {
    assertThrows(
        IllegalStateException.class,
        () ->
            cafe.woden.ircclient.app.outbound.TestBackendSupport
                .messageMutationOutboundCommandsRouter(
                    List.of(
                        new TestMessageMutationOutboundCommands("irc"),
                        new TestMessageMutationOutboundCommands("irc"))));
  }

  @Test
  void explicitBackendHandlerOverridesBuiltInRuntimeAdapter() {
    MessageMutationOutboundCommands custom = new TestMessageMutationOutboundCommands("irc");
    MessageMutationOutboundCommandsRouter router =
        cafe.woden.ircclient.app.outbound.TestBackendSupport.messageMutationOutboundCommandsRouter(
            List.of(custom));

    assertInstanceOf(TestMessageMutationOutboundCommands.class, router.commandsFor("irc"));
  }

  @Test
  void routesCustomBackendIds() {
    MessageMutationOutboundCommands pluginCommands =
        new TestMessageMutationOutboundCommands("plugin");
    MessageMutationOutboundCommandsRouter router =
        cafe.woden.ircclient.app.outbound.TestBackendSupport.messageMutationOutboundCommandsRouter(
            List.of(pluginCommands));

    assertInstanceOf(TestMessageMutationOutboundCommands.class, router.commandsFor("plugin"));
  }

  private static void assertMessageMutationCommands(
      MessageMutationOutboundCommands commands, String backendId) {
    MessageMutationTargetView target = new MessageMutationTargetView("server", "#ircafe");

    assertEquals(backendId, commands.backendId());
    assertEquals(
        "@+reply=reply-1 PRIVMSG #ircafe :hello",
        commands.buildReplyRawLine(target, "reply-1", "hello"));
    assertEquals(
        "@+draft/react=:;+reply=reply-1 TAGMSG #ircafe",
        commands.buildReactRawLine(target, "reply-1", ":"));
    assertEquals(
        "@+draft/unreact=:;+reply=reply-1 TAGMSG #ircafe",
        commands.buildUnreactRawLine(target, "reply-1", ":"));
    assertEquals(
        "@+draft/edit=msg-1 PRIVMSG #ircafe :edited",
        commands.buildEditRawLine(target, "msg-1", "edited"));
    assertEquals(
        "REDACT #ircafe msg-1 :cleanup",
        commands.buildRedactRawLine(target, "msg-1", "cleanup"));
  }

  private static final class TestMessageMutationOutboundCommands
      implements MessageMutationOutboundCommands {
    private final String backendId;

    private TestMessageMutationOutboundCommands(String backendId) {
      this.backendId = backendId;
    }

    @Override
    public String backendId() {
      return backendId;
    }

    @Override
    public String buildReplyRawLine(
        MessageMutationTargetView target, String replyToMessageId, String message) {
      return "test";
    }

    @Override
    public String buildReactRawLine(
        MessageMutationTargetView target, String replyToMessageId, String reaction) {
      return "test";
    }

    @Override
    public String buildUnreactRawLine(
        MessageMutationTargetView target, String replyToMessageId, String reaction) {
      return "test";
    }

    @Override
    public String buildEditRawLine(
        MessageMutationTargetView target, String targetMessageId, String editedText) {
      return "test";
    }

    @Override
    public String buildRedactRawLine(
        MessageMutationTargetView target, String targetMessageId, String reason) {
      return "test";
    }
  }
}
