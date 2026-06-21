package cafe.woden.ircclient.app.outbound.mutation.spi;

import java.util.Map;
import java.util.Objects;

/** Backend-specific payload shaping for reply/react/edit/redact outbound commands. */
public interface MessageMutationOutboundCommands {

  default String backendId() {
    return "";
  }

  String buildReplyRawLine(
      MessageMutationTargetView target, String replyToMessageId, String message);

  String buildReactRawLine(
      MessageMutationTargetView target, String replyToMessageId, String reaction);

  String buildUnreactRawLine(
      MessageMutationTargetView target, String replyToMessageId, String reaction);

  String buildEditRawLine(
      MessageMutationTargetView target, String targetMessageId, String editedText);

  String buildRedactRawLine(
      MessageMutationTargetView target, String targetMessageId, String reason);

  default Map<String, String> localEchoEditTags(String targetMessageId) {
    String msgId = Objects.toString(targetMessageId, "").trim();
    if (msgId.isEmpty()) return Map.of();
    return Map.of("draft/edit", msgId);
  }

  default Map<String, String> localEchoRedactionTags(String targetMessageId) {
    String msgId = Objects.toString(targetMessageId, "").trim();
    if (msgId.isEmpty()) return Map.of();
    return Map.of("draft/delete", msgId);
  }
}
