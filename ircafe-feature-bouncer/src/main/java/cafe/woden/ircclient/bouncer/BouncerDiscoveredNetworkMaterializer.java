package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BuiltInBouncerBackendIds;
import cafe.woden.ircclient.bouncer.spi.BuiltInBouncerNetworkNaming;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Builds normalized discovery events from protocol-specific parsed values. */
public final class BouncerDiscoveredNetworkMaterializer {

  public BouncerDiscoveredNetwork fromGenericProtocol(
      String originServerId, String networkId, Map<String, String> attributes) {
    Map<String, String> attrs = attributes == null ? Map.of() : attributes;

    String backend = firstNonBlank(attrs.get("backend"), BuiltInBouncerBackendIds.GENERIC);
    String displayName = firstNonBlank(attrs.get("name"), attrs.get("display"), networkId);
    String autoConnectName =
        firstNonBlank(attrs.get("auto"), attrs.get("autoconnect"), displayName);

    return materialize(
        backend,
        originServerId,
        networkId,
        displayName,
        autoConnectName,
        "generic-protocol",
        attrs);
  }

  public BouncerDiscoveredNetwork fromSojuNetwork(
      String originServerId,
      String networkId,
      String displayName,
      Map<String, String> attributes) {
    String effectiveDisplay = normalize(displayName);
    if (effectiveDisplay == null) {
      effectiveDisplay = "net-" + networkId;
    }

    return materialize(
        BuiltInBouncerBackendIds.SOJU,
        originServerId,
        networkId,
        effectiveDisplay,
        effectiveDisplay,
        BuiltInBouncerBackendIds.SOJU,
        attributes);
  }

  public BouncerDiscoveredNetwork fromZncListNetworksRow(
      String originServerId, String networkName, Boolean onIrc) {
    String displayName = normalize(networkName);
    if (displayName == null) return null;

    String networkId = BuiltInBouncerNetworkNaming.normalizeZncNetworkKey(displayName);
    if (networkId == null || networkId.isBlank()) {
      networkId = displayName.toLowerCase(Locale.ROOT);
    }

    HashMap<String, String> attributes = new HashMap<>();
    if (onIrc != null) {
      attributes.put("onIrc", String.valueOf(onIrc));
    }

    return materialize(
        BuiltInBouncerBackendIds.ZNC,
        originServerId,
        networkId,
        displayName,
        displayName,
        BuiltInBouncerBackendIds.ZNC,
        attributes);
  }

  private static BouncerDiscoveredNetwork materialize(
      String backendId,
      String originServerId,
      String networkId,
      String displayName,
      String autoConnectName,
      String source,
      Map<String, String> attributes) {
    HashMap<String, String> merged = new HashMap<>();
    if (attributes != null) {
      merged.putAll(attributes);
    }
    merged.put("source", source);

    return new BouncerDiscoveredNetwork(
        backendId,
        originServerId,
        networkId,
        displayName,
        autoConnectName,
        Map.copyOf(merged));
  }

  private static String firstNonBlank(String... values) {
    if (values == null) return null;
    for (String value : values) {
      String normalized = normalize(value);
      if (normalized != null) return normalized;
    }
    return null;
  }

  private static String normalize(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
