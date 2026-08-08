package cafe.woden.ircclient.config.runtime.ircv3;

import static cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3CapabilityCodec.isCapabilityEnabled;
import static cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3CapabilityCodec.normalizeCapabilityKey;
import static cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3CapabilityCodec.parseCapabilities;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.Ircv3CapabilityNameResolverPort;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigIrcv3CapabilityCodecTest {

  @Test
  void parseCapabilitiesNormalizesKeysAndKeepsOnlyBooleanValues() {
    Map<String, Object> raw = new LinkedHashMap<>();
    raw.put(" draft/chathistory ", false);
    raw.put("typing", "not-a-boolean");
    raw.put(" ", true);

    Map<String, Boolean> parsed = parseCapabilities(raw, null);

    assertEquals(Map.of("chathistory", false), parsed);
  }

  @Test
  void normalizeCapabilityKeyUsesRuntimeResolver() {
    Ircv3CapabilityNameResolverPort resolver =
        new Ircv3CapabilityNameResolverPort() {
          @Override
          public String normalizePreferenceKey(String capability) {
            return switch (String.valueOf(capability).trim().toLowerCase(Locale.ROOT)) {
              case "plugin/example-cap", "draft/plugin-example-cap" -> "plugin-example-cap";
              default -> Ircv3CapabilityNameResolverPort.super.normalizePreferenceKey(capability);
            };
          }
        };

    assertEquals(
        "plugin-example-cap", normalizeCapabilityKey(" draft/plugin-example-cap ", resolver));
    assertNull(normalizeCapabilityKey(" ", resolver));
  }

  @Test
  void isCapabilityEnabledUsesDefaultsForMissingOrInvalidKeys() {
    Map<String, Boolean> capabilities = Map.of("typing", false);

    assertFalse(isCapabilityEnabled(capabilities, "typing", true));
    assertTrue(isCapabilityEnabled(capabilities, "chathistory", true));
    assertFalse(isCapabilityEnabled(capabilities, null, false));
  }
}
