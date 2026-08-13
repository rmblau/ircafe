package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BuiltInBouncerNetworkNaming;
import java.util.Locale;
import java.util.Objects;

/** Feature-owned normalizer for persisted ZNC auto-connect network keys. */
public final class ZncAutoConnectNetworkKeyNormalizer {

  public String normalize(String networkName) {
    String value = BuiltInBouncerNetworkNaming.sanitizeZncNetworkSegment(networkName);
    value = Objects.toString(value, "").trim();
    if (value.isEmpty()) return null;

    value = value.replaceAll("_+", "_");
    while (value.startsWith("_")) value = value.substring(1);
    while (value.endsWith("_")) value = value.substring(0, value.length() - 1);

    value = value.trim();
    if (value.isEmpty()) return null;
    return value.toLowerCase(Locale.ROOT);
  }
}
