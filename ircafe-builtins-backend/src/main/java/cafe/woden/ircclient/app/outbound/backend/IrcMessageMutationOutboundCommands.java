package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendIds;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommandLines;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationTargetView;

/** IRC backend payload shaping for message mutation commands. */
public final class IrcMessageMutationOutboundCommands implements MessageMutationOutboundCommands {
  @Override
  public String backendId() {
    return BuiltInBackendIds.IRC;
  }

  @Override
  public String buildReplyRawLine(
      MessageMutationTargetView target, String replyToMessageId, String message) {
    return MessageMutationOutboundCommandLines.buildReplyRawLine(target, replyToMessageId, message);
  }

  @Override
  public String buildReactRawLine(
      MessageMutationTargetView target, String replyToMessageId, String reaction) {
    return MessageMutationOutboundCommandLines.buildReactRawLine(
        target, replyToMessageId, reaction);
  }

  @Override
  public String buildUnreactRawLine(
      MessageMutationTargetView target, String replyToMessageId, String reaction) {
    return MessageMutationOutboundCommandLines.buildUnreactRawLine(
        target, replyToMessageId, reaction);
  }

  @Override
  public String buildEditRawLine(
      MessageMutationTargetView target, String targetMessageId, String editedText) {
    return MessageMutationOutboundCommandLines.buildEditRawLine(
        target, targetMessageId, editedText);
  }

  @Override
  public String buildRedactRawLine(
      MessageMutationTargetView target, String targetMessageId, String reason) {
    return MessageMutationOutboundCommandLines.buildRedactRawLine(target, targetMessageId, reason);
  }
}
