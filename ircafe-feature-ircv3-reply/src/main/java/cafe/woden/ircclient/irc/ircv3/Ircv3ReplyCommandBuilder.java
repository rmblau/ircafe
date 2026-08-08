package cafe.woden.ircclient.irc.ircv3;

/** Builds outbound IRCv3 reply commands and input-field prefills. */
public final class Ircv3ReplyCommandBuilder {

  private Ircv3ReplyCommandBuilder() {}

  public static String buildRawLine(String target, String replyToMessageId, String message) {
    String outTarget = Ircv3CommandValuePolicy.normalizeTarget(target);
    String msgId = Ircv3CommandValuePolicy.normalizeTagValue(replyToMessageId);
    String text = Ircv3CommandValuePolicy.normalizeText(message);
    if (outTarget.isEmpty() || msgId.isEmpty() || text.isEmpty()) return "";
    return "@+reply="
        + Ircv3CommandValuePolicy.escapeTagValue(msgId)
        + " PRIVMSG "
        + outTarget
        + " :"
        + text;
  }

  public static String buildPrefillDraft(String target, String replyToMessageId) {
    String outTarget = Ircv3CommandValuePolicy.normalizeTarget(target);
    String msgId = Ircv3CommandValuePolicy.normalizeTagValue(replyToMessageId);
    if (outTarget.isEmpty() || msgId.isEmpty()) return "";
    return "/quote @+reply="
        + Ircv3CommandValuePolicy.escapeTagValue(msgId)
        + " PRIVMSG "
        + outTarget
        + " :";
  }
}
