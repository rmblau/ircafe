package cafe.woden.ircclient.config;

/** Shared Spring configuration property names and prefixes. */
public final class ConfigPropertyKeys {

  public static final String IRC_PREFIX = "irc";
  public static final String IRCAFE_PREFIX = "ircafe";
  public static final String IRCAFE_IGNORE_PREFIX = IRCAFE_PREFIX + ".ignore";
  public static final String IRCAFE_LOGGING_PREFIX = IRCAFE_PREFIX + ".logging";
  public static final String IRCAFE_PUSHY_PREFIX = IRCAFE_PREFIX + ".pushy";
  public static final String IRCAFE_SOJU_PREFIX = IRCAFE_PREFIX + ".soju";
  public static final String IRCAFE_UI_PREFIX = IRCAFE_PREFIX + ".ui";
  public static final String IRCAFE_ZNC_PREFIX = IRCAFE_PREFIX + ".znc";

  public static final String ENABLED_PROPERTY = "enabled";
  public static final String TRUE_VALUE = "true";
  public static final String FALSE_VALUE = "false";

  public static final String LOGGING_ENABLED = IRCAFE_LOGGING_PREFIX + "." + ENABLED_PROPERTY;
  public static final String LOGGING_ENABLED_TRUE = LOGGING_ENABLED + "=" + TRUE_VALUE;
  public static final String LOGGING_ENABLED_FALSE = LOGGING_ENABLED + "=" + FALSE_VALUE;
  public static final String LOGGING_WRITER_BATCH_SIZE = IRCAFE_LOGGING_PREFIX + ".writerBatchSize";
  public static final String LOGGING_WRITER_QUEUE_MAX = IRCAFE_LOGGING_PREFIX + ".writerQueueMax";

  private ConfigPropertyKeys() {}
}
