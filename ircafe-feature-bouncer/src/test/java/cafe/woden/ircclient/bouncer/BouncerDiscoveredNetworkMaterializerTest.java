package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BouncerDiscoveredNetworkMaterializerTest {

  private final BouncerDiscoveredNetworkMaterializer materializer =
      new BouncerDiscoveredNetworkMaterializer();

  @Test
  void materializesGenericFallbacksAndAttributeMetadata() {
    BouncerDiscoveredNetwork network =
        materializer.fromGenericProtocol(
            " origin ",
            "net-1",
            Map.of(
                "backend", " Generic ",
                "display", " Libera ",
                "autoconnect", " libera-auto ",
                "login", " alice/libera ",
                "caps", "message-tags, DRAFT/react",
                "source", "ignored"));

    assertEquals("generic", network.backendId());
    assertEquals("origin", network.originServerId());
    assertEquals("net-1", network.networkId());
    assertEquals("Libera", network.displayName());
    assertEquals("libera-auto", network.autoConnectName());
    assertEquals("alice/libera", network.loginUserHint());
    assertTrue(network.hasCapability("message-tags"));
    assertTrue(network.hasCapability("draft/react"));
    assertEquals("generic-protocol", network.attributes().get("source"));
  }

  @Test
  void materializesSojuFallbackNameAndOverridesSource() {
    BouncerDiscoveredNetwork network =
        materializer.fromSojuNetwork(
            "origin",
            "42",
            " ",
            Map.of(
                "source", "ignored",
                "loginUser", "alice/oftc",
                "capabilities", "echo-message batch"));

    assertEquals("soju", network.backendId());
    assertEquals("net-42", network.displayName());
    assertEquals("net-42", network.autoConnectName());
    assertEquals("alice/oftc", network.loginUserHint());
    assertTrue(network.hasCapability("echo-message"));
    assertTrue(network.hasCapability("batch"));
    assertEquals("soju", network.attributes().get("source"));
  }

  @Test
  void materializesZncNetworkKeyAndConnectionMetadata() {
    BouncerDiscoveredNetwork network =
        materializer.fromZncListNetworksRow("origin", " My Network! ", Boolean.TRUE);

    assertEquals("znc", network.backendId());
    assertEquals("my_network", network.networkId());
    assertEquals("My Network!", network.displayName());
    assertEquals("true", network.attributes().get("onIrc"));
    assertEquals("znc", network.attributes().get("source"));
  }

  @Test
  void rejectsBlankZncRowsBeforeConstructingAnEvent() {
    assertNull(materializer.fromZncListNetworksRow("origin", " ", Boolean.FALSE));
  }
}
