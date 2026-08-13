package cafe.woden.ircclient.irc.soju;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import org.junit.jupiter.api.Test;

class SojuBouncerDiscoveryAdapterTest {

  private final SojuBouncerDiscoveryAdapter adapter = new SojuBouncerDiscoveryAdapter();

  @Test
  void parsesProtocolValuesAndDelegatesDiscoveryMaterialization() {
    BouncerDiscoveredNetwork network =
        adapter.parseBouncerNetworkLine(
            "origin",
            ":srv BOUNCER NETWORK 123 name=Libera Chat;loginUser=alice/lib;caps=message-tags,draft/react");

    assertNotNull(network);
    assertEquals("soju", network.backendId());
    assertEquals("123", network.networkId());
    assertEquals("Libera_Chat", network.displayName());
    assertEquals("alice/lib", network.loginUserHint());
    assertTrue(network.hasCapability("message-tags"));
    assertTrue(network.hasCapability("draft/react"));
    assertEquals("soju", network.attributes().get("source"));
  }
}
