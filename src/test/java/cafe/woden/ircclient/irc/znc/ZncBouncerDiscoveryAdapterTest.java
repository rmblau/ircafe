package cafe.woden.ircclient.irc.znc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import org.junit.jupiter.api.Test;

class ZncBouncerDiscoveryAdapterTest {

  private final ZncBouncerDiscoveryAdapter adapter = new ZncBouncerDiscoveryAdapter();

  @Test
  void parsesProtocolValuesAndDelegatesDiscoveryMaterialization() {
    BouncerDiscoveredNetwork network =
        adapter.parseListNetworksRow("origin", "| Libera Chat | yes |");

    assertNotNull(network);
    assertEquals("znc", network.backendId());
    assertEquals("libera_chat", network.networkId());
    assertEquals("Libera Chat", network.displayName());
    assertEquals("true", network.attributes().get("onIrc"));
    assertEquals("znc", network.attributes().get("source"));
  }
}
