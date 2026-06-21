package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationTargetView;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.BackendDescriptorCatalog;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Quassel backend payload shaping for message mutation commands. */
@Component
@SecondaryAdapter
@ApplicationLayer
public final class QuasselMessageMutationOutboundCommands
    implements MessageMutationOutboundCommands {
  private static final BackendDescriptorCatalog BACKEND_DESCRIPTORS =
      BackendDescriptorCatalog.builtIns();

  @Override
  public String backendId() {
    return BACKEND_DESCRIPTORS.idFor(IrcProperties.Server.Backend.QUASSEL_CORE);
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
