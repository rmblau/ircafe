package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigNickColorSettingsCodec.normalizeMinContrast;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigNickColorSettingsCodec.serializeOverrides;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigNickColorSettingsCodecTest {

  @Test
  void normalizeMinContrastKeepsPositiveValuesAndDefaultsInvalidRanges() {
    assertEquals(4.5, normalizeMinContrast(4.5));
    assertEquals(3.0, normalizeMinContrast(0));
    assertEquals(3.0, normalizeMinContrast(-1));
    assertEquals(3.0, normalizeMinContrast(Double.NaN));
  }

  @Test
  void serializeOverridesTrimsEntriesAndSkipsBlankValues() {
    Map<String, String> overrides = new LinkedHashMap<>();
    overrides.put(" alice ", " #123456 ");
    overrides.put(" ", "#ffffff");
    overrides.put("bob", " ");
    overrides.put(null, "#000000");
    overrides.put("carol", null);
    overrides.put(" dave ", " blue ");

    Map<String, Object> serialized = serializeOverrides(overrides);

    assertEquals(Map.of("alice", "#123456", "dave", "blue"), serialized);
    assertEquals(List.of("alice", "dave"), new ArrayList<>(serialized.keySet()));
  }

  @Test
  void serializeOverridesTreatsNullAsNoEntries() {
    assertTrue(serializeOverrides(null).isEmpty());
  }
}
