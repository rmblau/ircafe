package cafe.woden.ircclient.irc.ircv3.spi;

/** Known parsed IRC command interpretation operations exposed to runtime plugins. */
public enum Ircv3InboundCommandOperation {
  AWAY_NOTIFY,
  ACCOUNT_NOTIFY,
  EXTENDED_JOIN,
  CHGHOST,
  SETNAME,
  INVITE_NOTIFY,
  /** @deprecated Use the focused capability operation constants. */
  @Deprecated
  PRESENCE,
  /** @deprecated Use {@link #CHGHOST} or {@link #SETNAME}. */
  @Deprecated
  IDENTITY_CHANGE,
  STANDARD_REPLY,
  MONITOR,
  USERHOST,
  WHOIS_AWAY,
  WHOIS_ACCOUNT,
  WHOIS_END,
  WHOIS_USER,
  WHO,
  WHOX,
  READ_MARKER,
  MESSAGE_REDACTION,
  HISTORY_BATCH_CONTROL,
  HISTORY_ZNC_CAPABILITY,
  HISTORY_ZNC_RPL004,
  MULTILINE_CAPABILITY_STATE,
  CAP_NEGOTIATION,
  ISUPPORT_TOKENS,
  ISUPPORT_WHOX,
  ISUPPORT_MONITOR,
  ISUPPORT_CLIENT_TAG_POLICY,
  STS_CAPABILITY,
  SASL_CAPABILITY_LIST,
  SASL_CAPABILITY_ACK,
  SASL_CAPABILITY_NAK,
  SASL_SERVER_MESSAGE,
  SASL_FAILURE
}
