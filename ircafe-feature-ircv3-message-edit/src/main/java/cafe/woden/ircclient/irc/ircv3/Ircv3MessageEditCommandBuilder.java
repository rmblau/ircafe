package cafe.woden.ircclient.irc.ircv3;

/** Builds outbound experimental IRCv3 message-edit commands. */
public final class Ircv3MessageEditCommandBuilder {

  private Ircv3MessageEditCommandBuilder() {}

  public static String buildRawLine(String target, String targetMessageId, String editedText) {
    String outTarget = Ircv3CommandValuePolicy.normalizeTarget(target);
    String msgId = Ircv3CommandValuePolicy.normalizeTagValue(targetMessageId);
    String text = Ircv3CommandValuePolicy.normalizeText(editedText);
    if (outTarget.isEmpty() || msgId.isEmpty() || text.isEmpty()) return "";
    return "@+draft/edit="
        + Ircv3CommandValuePolicy.escapeTagValue(msgId)
        + " PRIVMSG "
        + outTarget
        + " :"
        + text;
  }
}
