package cafe.woden.ircclient.config.runtime.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RuntimeConfigBouncerDiscoveryCodecTest {

  @Test
  void normalizesBackendKeysGenericKeysAndLoginTemplates() {
    assertEquals("soju", RuntimeConfigBouncerDiscoveryCodec.normalizeBackendKey(" Soju "));
    assertEquals("network", RuntimeConfigBouncerDiscoveryCodec.normalizeKey(" network "));
    assertEquals(
        "{base}/{network}",
        RuntimeConfigBouncerDiscoveryCodec.normalizeGenericBouncerLoginTemplate(" "));
    assertEquals(
        "{base}|{network}",
        RuntimeConfigBouncerDiscoveryCodec.normalizeGenericBouncerLoginTemplate(
            " {base}|{network} "));
  }

  @Test
  void readBooleanUsesRuntimeConfigBooleanCoercion() {
    assertEquals(Optional.of(true), RuntimeConfigBouncerDiscoveryCodec.readBoolean(1));
    assertEquals(Optional.of(false), RuntimeConfigBouncerDiscoveryCodec.readBoolean("false"));
    assertTrue(RuntimeConfigBouncerDiscoveryCodec.readBoolean("maybe").isEmpty());
  }

  @Test
  void autoConnectMutationAddsAndRemovesNetworksCaseInsensitively() {
    Map<String, Object> networks = new LinkedHashMap<>();

    RuntimeConfigBouncerDiscoveryCodec.mutateAutoConnectNetwork(networks, " Libera ", true);
    RuntimeConfigBouncerDiscoveryCodec.mutateAutoConnectNetwork(networks, "libera", false);

    assertTrue(networks.isEmpty());
  }

  @Test
  void readAutoConnectRulesKeepsEnabledNetworkEntriesOnly() {
    assertEquals(
        Map.of("bouncer-1", Map.of("Libera", true, "OFTC", true)),
        RuntimeConfigBouncerDiscoveryCodec.readAutoConnectRules(
            Map.of(
                " bouncer-1 ",
                Map.of(
                    " Libera ",
                    true,
                    "Disabled",
                    false,
                    "Invalid",
                    List.of("not-bool"),
                    "OFTC",
                    "true"),
                "blank",
                Map.of("", true),
                "",
                Map.of("ignored", true),
                "bad",
                "not-a-map")));
  }
}
