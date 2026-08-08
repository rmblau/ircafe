package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingContext;

/** Feature-owned builder for mapping-strategy runtime context snapshots. */
public final class BouncerNetworkMappingContextFactory {

  public String defaultGenericLoginTemplate() {
    return BouncerNetworkMappingContext.DEFAULT_GENERIC_LOGIN_TEMPLATE;
  }

  public boolean defaultPreferLoginHint() {
    return BouncerNetworkMappingContext.DEFAULT_PREFER_LOGIN_HINT;
  }

  public BouncerNetworkMappingContext defaults() {
    return BouncerNetworkMappingContext.defaults();
  }

  public BouncerNetworkMappingContext fromRuntimeSettings(
      String genericLoginTemplate, boolean preferLoginHint) {
    return new BouncerNetworkMappingContext(genericLoginTemplate, preferLoginHint);
  }
}
