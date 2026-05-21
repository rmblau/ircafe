package cafe.woden.ircclient.irc.ircv3;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** IRCv3 CHATHISTORY selector key prefixes. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Ircv3ChatHistorySelectors {

  public static final String MSGID_PREFIX = "msgid=";
  public static final String TIMESTAMP_PREFIX = "timestamp=";
}
