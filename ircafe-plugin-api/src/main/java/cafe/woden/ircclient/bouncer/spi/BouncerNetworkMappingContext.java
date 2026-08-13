package cafe.woden.ircclient.bouncer.spi;

import java.util.Objects;

/** App-provided runtime policy snapshot for bouncer network mapping strategies. */
public record BouncerNetworkMappingContext(String genericLoginTemplate, boolean preferLoginHint) {

  public static final String DEFAULT_GENERIC_LOGIN_TEMPLATE = "{base}/{network}";
  public static final boolean DEFAULT_PREFER_LOGIN_HINT = true;

  public BouncerNetworkMappingContext {
    genericLoginTemplate = normalize(genericLoginTemplate);
    if (genericLoginTemplate == null) {
      genericLoginTemplate = DEFAULT_GENERIC_LOGIN_TEMPLATE;
    }
  }

  public static BouncerNetworkMappingContext defaults() {
    return new BouncerNetworkMappingContext(
        DEFAULT_GENERIC_LOGIN_TEMPLATE, DEFAULT_PREFER_LOGIN_HINT);
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v;
  }
}
