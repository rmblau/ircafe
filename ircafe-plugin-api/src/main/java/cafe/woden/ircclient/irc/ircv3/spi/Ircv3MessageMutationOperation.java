package cafe.woden.ircclient.irc.ircv3.spi;

/** Outbound IRCv3 message-mutation operation supplied by a runtime provider. */
public enum Ircv3MessageMutationOperation {
  REPLY,
  REACT,
  UNREACT,
  EDIT,
  REDACT
}
