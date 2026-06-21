package cafe.woden.ircclient.app.outbound.backend;

import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_REACT;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_UNREACT;

import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationTargetView;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Shared IRCv3 raw-line builders for message mutation command handlers. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MessageMutationOutboundCommandLineBuilder {

  static String buildReplyRawLine(
      MessageMutationTargetView target, String replyToMessageId, String message) {
    String outTarget = normalizeTarget(target);
    String msgId = normalizeToken(replyToMessageId);
    String text = normalizeText(message);
    if (outTarget.isEmpty() || msgId.isEmpty() || text.isEmpty()) return "";
    return "@+reply=" + escapeIrcv3TagValue(msgId) + " PRIVMSG " + outTarget + " :" + text;
  }

  static String buildReactRawLine(
      MessageMutationTargetView target, String replyToMessageId, String reaction) {
    return buildReactionRawLine(target, replyToMessageId, reaction, DRAFT_REACT);
  }

  static String buildUnreactRawLine(
      MessageMutationTargetView target, String replyToMessageId, String reaction) {
    return buildReactionRawLine(target, replyToMessageId, reaction, DRAFT_UNREACT);
  }

  static String buildEditRawLine(
      MessageMutationTargetView target, String targetMessageId, String editedText) {
    String outTarget = normalizeTarget(target);
    String msgId = normalizeToken(targetMessageId);
    String text = normalizeText(editedText);
    if (outTarget.isEmpty() || msgId.isEmpty() || text.isEmpty()) return "";
    return "@+draft/edit=" + escapeIrcv3TagValue(msgId) + " PRIVMSG " + outTarget + " :" + text;
  }

  static String buildRedactRawLine(
      MessageMutationTargetView target, String targetMessageId, String reason) {
    String outTarget = normalizeTarget(target);
    String msgId = normalizeToken(targetMessageId);
    if (outTarget.isEmpty() || msgId.isEmpty()) return "";
    String why = normalizeTextAllowBlank(reason);
    return why.isEmpty()
        ? ("REDACT " + outTarget + " " + msgId)
        : ("REDACT " + outTarget + " " + msgId + " :" + why);
  }

  private static String buildReactionRawLine(
      MessageMutationTargetView target,
      String replyToMessageId,
      String reaction,
      String reactionTagKey) {
    String outTarget = normalizeTarget(target);
    String msgId = normalizeToken(replyToMessageId);
    String react = normalizeToken(reaction);
    String tagKey = normalizeToken(reactionTagKey);
    if (outTarget.isEmpty() || msgId.isEmpty() || react.isEmpty() || tagKey.isEmpty()) return "";
    return "@+"
        + tagKey
        + "="
        + escapeIrcv3TagValue(react)
        + ";+reply="
        + escapeIrcv3TagValue(msgId)
        + " TAGMSG "
        + outTarget;
  }

  private static String normalizeTarget(MessageMutationTargetView target) {
    if (target == null) return "";
    return Objects.toString(target.target(), "").trim();
  }

  private static String normalizeText(String value) {
    return Objects.toString(value, "").trim();
  }

  private static String normalizeTextAllowBlank(String value) {
    return Objects.toString(value, "").trim();
  }

  private static String normalizeToken(String value) {
    String token = Objects.toString(value, "").trim();
    if (token.isEmpty()) return "";
    if (token.indexOf(' ') >= 0 || token.indexOf('\n') >= 0 || token.indexOf('\r') >= 0) return "";
    return token;
  }

  private static String escapeIrcv3TagValue(String value) {
    String raw = Objects.toString(value, "");
    if (raw.isEmpty()) return "";
    StringBuilder out = new StringBuilder(raw.length() + 8);
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      switch (c) {
        case ';' -> out.append("\\:");
        case ' ' -> out.append("\\s");
        case '\\' -> out.append("\\\\");
        case '\r' -> out.append("\\r");
        case '\n' -> out.append("\\n");
        default -> out.append(c);
      }
    }
    return out.toString();
  }
}
