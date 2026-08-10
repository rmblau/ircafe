package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationTargetView;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageMutationRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageMutationRuntimeSupport;
import java.util.Locale;
import java.util.Objects;

/** Built-in backend adapter that routes message mutations through runtime IRCv3 SPI providers. */
public final class Ircv3MessageMutationOutboundCommands implements MessageMutationOutboundCommands {

  private final String backendId;
  private final Ircv3MessageMutationRuntimeSupport runtimeSupport;

  public Ircv3MessageMutationOutboundCommands(
      String backendId, Ircv3MessageMutationRuntimeCatalog runtimeCatalog) {
    this(backendId, Ircv3MessageMutationRuntimeSupport.outboundOnly(runtimeCatalog));
  }

  Ircv3MessageMutationOutboundCommands(
      String backendId, Ircv3MessageMutationRuntimeSupport runtimeSupport) {
    this.backendId = Objects.toString(backendId, "").trim().toLowerCase(Locale.ROOT);
    if (this.backendId.isEmpty()) {
      throw new IllegalArgumentException("backendId must not be blank");
    }
    this.runtimeSupport = Objects.requireNonNull(runtimeSupport, "runtimeSupport");
  }

  @Override
  public String backendId() {
    return backendId;
  }

  @Override
  public String buildReplyRawLine(
      MessageMutationTargetView target, String replyToMessageId, String message) {
    return runtimeSupport
        .renderReply(targetValue(target), replyToMessageId, message)
        .map(Ircv3MessageMutationRuntimeSupport.OutboundPlan::rawLine)
        .orElse("");
  }

  @Override
  public String buildReactRawLine(
      MessageMutationTargetView target, String replyToMessageId, String reaction) {
    return runtimeSupport
        .renderReaction(targetValue(target), replyToMessageId, reaction, false)
        .map(Ircv3MessageMutationRuntimeSupport.OutboundPlan::rawLine)
        .orElse("");
  }

  @Override
  public String buildUnreactRawLine(
      MessageMutationTargetView target, String replyToMessageId, String reaction) {
    return runtimeSupport
        .renderReaction(targetValue(target), replyToMessageId, reaction, true)
        .map(Ircv3MessageMutationRuntimeSupport.OutboundPlan::rawLine)
        .orElse("");
  }

  @Override
  public String buildEditRawLine(
      MessageMutationTargetView target, String targetMessageId, String editedText) {
    return runtimeSupport
        .renderEdit(targetValue(target), targetMessageId, editedText)
        .map(Ircv3MessageMutationRuntimeSupport.OutboundPlan::rawLine)
        .orElse("");
  }

  @Override
  public String buildRedactRawLine(
      MessageMutationTargetView target, String targetMessageId, String reason) {
    return runtimeSupport
        .renderRedaction(targetValue(target), targetMessageId, reason)
        .map(Ircv3MessageMutationRuntimeSupport.OutboundPlan::rawLine)
        .orElse("");
  }

  private static String targetValue(MessageMutationTargetView target) {
    return target == null ? "" : Objects.toString(target.target(), "");
  }
}
