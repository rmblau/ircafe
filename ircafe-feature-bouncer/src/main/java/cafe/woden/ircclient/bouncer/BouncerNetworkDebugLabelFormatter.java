package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import java.util.Objects;

/** Feature-owned formatting policy for optional discovered-network debug labels. */
public final class BouncerNetworkDebugLabelFormatter {

  public String suffixFor(
      BouncerNetworkMappingStrategy mappingStrategy, BouncerDiscoveredNetwork network) {
    Objects.requireNonNull(mappingStrategy, "mappingStrategy");
    Objects.requireNonNull(network, "network");
    return suffixFor(mappingStrategy.networkDebugId(network));
  }

  public String suffixFor(String debugId) {
    String debug = normalize(debugId);
    return debug == null ? "" : " (" + debug + ")";
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v;
  }
}
