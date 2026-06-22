package cafe.woden.ircclient.bouncer.spi;

/** Stable bouncer backend IDs used by built-in bouncer providers and plugin integrations. */
public final class BuiltInBouncerBackendIds {
  public static final String GENERIC = "generic";
  public static final String SOJU = "soju";
  public static final String ZNC = "znc";

  private BuiltInBouncerBackendIds() {}
}
