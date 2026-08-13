package cafe.woden.ircclient.irc.ircv3;

import java.util.Map;

/** Transport-neutral message-ID alias selection policy. */
public final class Ircv3MessageIdTagPolicy {

  private Ircv3MessageIdTagPolicy() {}

  public static String firstMessageId(Map<String, String> tags) {
    return Ircv3Tags.firstTagValue(
        tags, "msgid", "+msgid", "draft/msgid", "+draft/msgid", "znc.in/msgid", "+znc.in/msgid");
  }
}
