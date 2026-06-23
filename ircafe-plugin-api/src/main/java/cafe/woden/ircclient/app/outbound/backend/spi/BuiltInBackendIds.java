package cafe.woden.ircclient.app.outbound.backend.spi;

/** Stable backend IDs used by built-in backend providers and plugin integrations. */
public final class BuiltInBackendIds {
  public static final String IRC = "irc";
  public static final String QUASSEL_CORE = "quassel-core";
  public static final String MATRIX = "matrix";

  private BuiltInBackendIds() {}
}
