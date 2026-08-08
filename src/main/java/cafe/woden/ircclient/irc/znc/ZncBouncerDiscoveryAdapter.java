package cafe.woden.ircclient.irc.znc;

import cafe.woden.ircclient.bouncer.BouncerDiscoveredNetworkMaterializer;
import cafe.woden.ircclient.bouncer.ZncBouncerListNetworksParser;
import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;

/** Translates ZNC *status ListNetworks lines into generic bouncer discovery events. */
public class ZncBouncerDiscoveryAdapter {

  private final ZncBouncerListNetworksParser protocolParser =
      new ZncBouncerListNetworksParser();
  private final BouncerDiscoveredNetworkMaterializer materializer =
      new BouncerDiscoveredNetworkMaterializer();

  public BouncerDiscoveredNetwork parseListNetworksRow(String originServerId, String messageText) {
    ZncBouncerListNetworksParser.ParsedRow row = protocolParser.parseRow(messageText);
    if (row == null) return null;

    return materializer.fromZncListNetworksRow(originServerId, row.name(), row.onIrc());
  }

  public boolean looksLikeListNetworksDoneLine(String messageText) {
    return protocolParser.isDoneLine(messageText);
  }
}
