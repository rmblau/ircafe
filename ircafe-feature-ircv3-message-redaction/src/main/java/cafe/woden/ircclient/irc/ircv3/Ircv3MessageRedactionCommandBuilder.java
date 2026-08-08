package cafe.woden.ircclient.irc.ircv3;

/** Builds outbound IRCv3 message-redaction commands. */
public final class Ircv3MessageRedactionCommandBuilder {

  private Ircv3MessageRedactionCommandBuilder() {}

  public static String buildRawLine(String target, String targetMessageId, String reason) {
    String outTarget = Ircv3CommandValuePolicy.normalizeTarget(target);
    String msgId = Ircv3CommandValuePolicy.normalizeToken(targetMessageId);
    if (outTarget.isEmpty() || msgId.isEmpty()) return "";
    String why = Ircv3CommandValuePolicy.normalizeText(reason);
    return why.isEmpty()
        ? ("REDACT " + outTarget + " " + msgId)
        : ("REDACT " + outTarget + " " + msgId + " :" + why);
  }
}
