package cafe.woden.ircclient.irc.soju;

import cafe.woden.ircclient.bouncer.BouncerDiscoveredNetworkMaterializer;
import cafe.woden.ircclient.bouncer.SojuBouncerProtocolParser;
import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;

/** Translates soju protocol discovery lines into generic bouncer discovery events. */
public class SojuBouncerDiscoveryAdapter {

  private final SojuBouncerProtocolParser protocolParser = new SojuBouncerProtocolParser();
  private final BouncerDiscoveredNetworkMaterializer materializer =
      new BouncerDiscoveredNetworkMaterializer();

  public BouncerDiscoveredNetwork parseBouncerNetworkLine(String originServerId, String rawLine) {
    SojuBouncerProtocolParser.ParsedNetwork parsed = protocolParser.parseNetworkLine(rawLine);
    if (parsed == null) return null;

    return materializer.fromSojuNetwork(
        originServerId, parsed.networkId(), parsed.name(), parsed.attributes());
  }
}
