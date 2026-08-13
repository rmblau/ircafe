package cafe.woden.ircclient.irc.ircv3.spi;

/** Runtime-rendered outbound IRCv3 command families. */
public enum Ircv3OutboundCommandOperation {
  TYPING,
  READ_MARKER,
  CHAT_HISTORY_BEFORE,
  CHAT_HISTORY_LATEST,
  CHAT_HISTORY_BETWEEN,
  CHAT_HISTORY_AROUND,
  MULTILINE,
  ZNC_PLAYBACK,
  LABELED_RESPONSE,
  MONITOR_LIST,
  MONITOR_STATUS,
  MONITOR_CLEAR,
  MONITOR_ADD,
  MONITOR_REMOVE
}
