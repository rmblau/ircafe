package cafe.woden.ircclient.app.outbound.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationTargetView;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.BackendDescriptorCatalog;
import java.util.List;
import org.junit.jupiter.api.Test;

class MessageMutationOutboundCommandsRouterTest {
  private static final BackendDescriptorCatalog BACKEND_DESCRIPTORS =
      BackendDescriptorCatalog.builtIns();

  @Test
  void routesToBackendSpecificHandlers() {
    MessageMutationOutboundCommandsRouter router =
        cafe.woden.ircclient.app.outbound.TestBackendSupport.messageMutationOutboundCommandsRouter(
            List.of(
                new IrcMessageMutationOutboundCommands(),
                new MatrixMessageMutationOutboundCommands(),
                new QuasselMessageMutationOutboundCommands()));

    assertInstanceOf(IrcMessageMutationOutboundCommands.class, router.commandsFor("irc"));
    assertInstanceOf(MatrixMessageMutationOutboundCommands.class, router.commandsFor("matrix"));
    assertInstanceOf(
        QuasselMessageMutationOutboundCommands.class, router.commandsFor("quassel-core"));
  }

  @Test
  void fallsBackToIrcHandlerWhenBackendHasNoRegisteredHandler() {
    MessageMutationOutboundCommandsRouter router =
        cafe.woden.ircclient.app.outbound.TestBackendSupport.messageMutationOutboundCommandsRouter(
            List.of(
                new IrcMessageMutationOutboundCommands(),
                new MatrixMessageMutationOutboundCommands()));

    assertIrcMessageMutationCommands(router.commandsFor(""));
    assertIrcMessageMutationCommands(router.commandsFor("quassel-core"));
  }

  @Test
  void rejectsDuplicateBackendHandlers() {
    assertThrows(
        IllegalStateException.class,
        () ->
            cafe.woden.ircclient.app.outbound.TestBackendSupport
                .messageMutationOutboundCommandsRouter(
                    List.of(
                        new IrcMessageMutationOutboundCommands(),
                        new DuplicateIrcMessageMutationOutboundCommands())));
  }

  @Test
  void defaultsToBuiltInIrcHandlerWhenCatalogHasNoExplicitIrcHandler() {
    MessageMutationOutboundCommandsRouter router =
        cafe.woden.ircclient.app.outbound.TestBackendSupport.messageMutationOutboundCommandsRouter(
            List.of(new MatrixMessageMutationOutboundCommands()));

    assertIrcMessageMutationCommands(router.commandsFor("irc"));
  }

  @Test
  void routesCustomBackendIds() {
    MessageMutationOutboundCommands pluginCommands = new PluginMessageMutationOutboundCommands();
    MessageMutationOutboundCommandsRouter router =
        cafe.woden.ircclient.app.outbound.TestBackendSupport.messageMutationOutboundCommandsRouter(
            List.of(new IrcMessageMutationOutboundCommands(), pluginCommands));

    assertInstanceOf(PluginMessageMutationOutboundCommands.class, router.commandsFor("plugin"));
  }

  private static void assertIrcMessageMutationCommands(MessageMutationOutboundCommands commands) {
    MessageMutationTargetView target = new MessageMutationTargetView("server", "#ircafe");

    assertEquals("irc", commands.backendId());
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
        "REDACT #ircafe msg-1 :cleanup", commands.buildRedactRawLine(target, "msg-1", "cleanup"));
  }

  private static final class DuplicateIrcMessageMutationOutboundCommands
      implements MessageMutationOutboundCommands {

    @Override
    public String backendId() {
      return BACKEND_DESCRIPTORS.idFor(IrcProperties.Server.Backend.IRC);
    }

    @Override
    public String buildReplyRawLine(
        MessageMutationTargetView target, String replyToMessageId, String message) {
      return "";
    }

    @Override
    public String buildReactRawLine(
        MessageMutationTargetView target, String replyToMessageId, String reaction) {
      return "";
    }

    @Override
    public String buildUnreactRawLine(
        MessageMutationTargetView target, String replyToMessageId, String reaction) {
      return "";
    }

    @Override
    public String buildEditRawLine(
        MessageMutationTargetView target, String targetMessageId, String editedText) {
      return "";
    }

    @Override
    public String buildRedactRawLine(
        MessageMutationTargetView target, String targetMessageId, String reason) {
      return "";
    }
  }

  private static final class PluginMessageMutationOutboundCommands
      implements MessageMutationOutboundCommands {
    @Override
    public String backendId() {
      return "plugin";
    }

    @Override
    public String buildReplyRawLine(
        MessageMutationTargetView target, String replyToMessageId, String message) {
      return "";
    }

    @Override
    public String buildReactRawLine(
        MessageMutationTargetView target, String replyToMessageId, String reaction) {
      return "";
    }

    @Override
    public String buildUnreactRawLine(
        MessageMutationTargetView target, String replyToMessageId, String reaction) {
      return "";
    }

    @Override
    public String buildEditRawLine(
        MessageMutationTargetView target, String targetMessageId, String editedText) {
      return "";
    }

    @Override
    public String buildRedactRawLine(
        MessageMutationTargetView target, String targetMessageId, String reason) {
      return "";
    }
  }
}
