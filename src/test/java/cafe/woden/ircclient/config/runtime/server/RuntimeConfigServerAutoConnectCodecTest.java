package cafe.woden.ircclient.config.runtime.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigServerAutoConnectCodecTest {

  @Test
  void readBooleanMapTrimsServerIdsAndIgnoresInvalidValues() {
    assertEquals(
        Map.of("libera", false, "oftc", true),
        RuntimeConfigServerAutoConnectCodec.readBooleanMap(
            Map.of(" libera ", false, "oftc", "true", "bad", "not-a-boolean", "", false)));
  }

  @Test
  void resolveServerAutoConnectHandlesExactCaseInsensitiveAndDefaultValues() {
    Map<String, Boolean> byServer = Map.of("Libera", false, "oftc", true);

    assertFalse(
        RuntimeConfigServerAutoConnectCodec.resolveServerAutoConnect(byServer, "Libera", true));
    assertFalse(
        RuntimeConfigServerAutoConnectCodec.resolveServerAutoConnect(byServer, "libera", true));
    assertTrue(
        RuntimeConfigServerAutoConnectCodec.resolveServerAutoConnect(byServer, "oftc", false));
    assertTrue(
        RuntimeConfigServerAutoConnectCodec.resolveServerAutoConnect(byServer, "missing", true));
    assertFalse(RuntimeConfigServerAutoConnectCodec.resolveServerAutoConnect(byServer, " ", false));
  }
}
