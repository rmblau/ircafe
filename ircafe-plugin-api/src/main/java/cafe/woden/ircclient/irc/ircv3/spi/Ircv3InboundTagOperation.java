package cafe.woden.ircclient.irc.ircv3.spi;

/** Known inbound IRCv3 tag interpretation operations exposed to runtime plugins. */
public enum Ircv3InboundTagOperation {
  CHANNEL_CONTEXT,
  REPLY,
  REACTIONS,
  MESSAGE_REDACTION,
  TYPING,
  READ_MARKER,
  MESSAGE_EDIT,
  ACCOUNT_TAG,
  ECHO_MESSAGE_TARGET_HINT,
  HISTORY_BATCH_REFERENCE,
  HISTORY_BOOTSTRAP_SUPPRESSION,
  LABELED_RESPONSE,
  SERVER_TIME,
  SERVER_TIME_LAG,
  MESSAGE_ID
}
