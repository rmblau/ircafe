package cafe.woden.ircclient.irc.ircv3;

/** Builds outbound IRCv3 reaction and reaction-removal commands. */
public final class Ircv3ReactionCommandBuilder {

  private static final String DRAFT_REACT = "draft/react";
  private static final String DRAFT_UNREACT = "draft/unreact";
  private static final String DEFAULT_REACTION = ":+1:";

  private Ircv3ReactionCommandBuilder() {}

  public static String buildReactRawLine(String target, String messageId, String reaction) {
    return buildRawLine(target, messageId, reaction, DRAFT_REACT);
  }

  public static String buildUnreactRawLine(String target, String messageId, String reaction) {
    return buildRawLine(target, messageId, reaction, DRAFT_UNREACT);
  }

  public static String buildReactPrefillDraft(String target, String messageId) {
    String raw = buildReactRawLine(target, messageId, DEFAULT_REACTION);
    return raw.isEmpty() ? "" : "/quote " + raw;
  }

  private static String buildRawLine(
      String target, String messageId, String reaction, String reactionTagKey) {
    String outTarget = Ircv3CommandValuePolicy.normalizeTarget(target);
    String msgId = Ircv3CommandValuePolicy.normalizeTagValue(messageId);
    String react = Ircv3CommandValuePolicy.normalizeTagValue(reaction);
    if (outTarget.isEmpty() || msgId.isEmpty() || react.isEmpty()) return "";
    return "@+"
        + reactionTagKey
        + "="
        + Ircv3CommandValuePolicy.escapeTagValue(react)
        + ";+reply="
        + Ircv3CommandValuePolicy.escapeTagValue(msgId)
        + " TAGMSG "
        + outTarget;
  }
}
