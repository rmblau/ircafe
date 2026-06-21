package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendIds;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationTargetView;

/** Matrix backend payload shaping for message mutation commands. */
public final class MatrixMessageMutationOutboundCommands
    implements MessageMutationOutboundCommands {

  @Override
  public String backendId() {
    return BuiltInBackendIds.MATRIX;
  }

  @Override
  public String buildReplyRawLine(
      MessageMutationTargetView target, String replyToMessageId, String message) {
    return MessageMutationOutboundCommandLineBuilder.buildReplyRawLine(
        target, replyToMessageId, message);
  }

  @Override
  public String buildReactRawLine(
      MessageMutationTargetView target, String replyToMessageId, String reaction) {
    return MessageMutationOutboundCommandLineBuilder.buildReactRawLine(
        target, replyToMessageId, reaction);
  }

  @Override
  public String buildUnreactRawLine(
      MessageMutationTargetView target, String replyToMessageId, String reaction) {
    return MessageMutationOutboundCommandLineBuilder.buildUnreactRawLine(
        target, replyToMessageId, reaction);
  }

  @Override
  public String buildEditRawLine(
      MessageMutationTargetView target, String targetMessageId, String editedText) {
    return MessageMutationOutboundCommandLineBuilder.buildEditRawLine(
        target, targetMessageId, editedText);
  }

  @Override
  public String buildRedactRawLine(
      MessageMutationTargetView target, String targetMessageId, String reason) {
    return MessageMutationOutboundCommandLineBuilder.buildRedactRawLine(
        target, targetMessageId, reason);
  }
}
