package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;
import org.junit.jupiter.api.Test;

class SettingsColorSupportTest {

  @Test
  void normalizesStrictSixDigitHexColors() {
    assertEquals("#1A2B3C", SettingsColorSupport.normalizeHexColor(" 0x1a2b3c "));
    assertEquals("#AABBCC", SettingsColorSupport.normalizeHexColor(" #aabbcc "));
    assertNull(SettingsColorSupport.normalizeHexColor("abc"));
    assertNull(SettingsColorSupport.normalizeHexColor("not-a-color"));
    assertNull(SettingsColorSupport.normalizeHexColor(null));
  }

  @Test
  void normalizesLenientShortHexColors() {
    assertEquals("#AABBCC", SettingsColorSupport.normalizeHexColorLenient("abc"));
    assertEquals("#AABBCC", SettingsColorSupport.normalizeHexColorLenient("#abc"));
    assertEquals("#AABBCC", SettingsColorSupport.normalizeHexColorLenient("0xabc"));
  }

  @Test
  void parsesNormalizedHexColors() {
    assertEquals(new Color(0x1A2B3C), SettingsColorSupport.parseHexColor("0x1a2b3c"));
    assertEquals(new Color(0xAABBCC), SettingsColorSupport.parseHexColorLenient("#abc"));
  }

  @Test
  void calculatesColorContrastHelpers() {
    assertEquals(Color.WHITE, SettingsColorSupport.bestTextColor(Color.BLACK));
    assertEquals(Color.BLACK, SettingsColorSupport.bestTextColor(Color.WHITE));
    assertEquals(Color.WHITE, SettingsColorSupport.bestTextColor(null));
    assertEquals(21.0, SettingsColorSupport.contrastRatio(Color.BLACK, Color.WHITE), 0.001);
    assertEquals(0.0, SettingsColorSupport.relativeLuminance(Color.BLACK), 0.001);
    assertEquals(1.0, SettingsColorSupport.relativeLuminance(Color.WHITE), 0.001);
    assertEquals(0.0, SettingsColorSupport.contrastRatio(null, Color.WHITE), 0.001);
    assertEquals(true, SettingsColorSupport.isDark(Color.BLACK));
    assertEquals(false, SettingsColorSupport.isDark(Color.WHITE));
  }

  @Test
  void normalizesOptionalHexForApply() {
    assertNull(SettingsColorSupport.normalizeOptionalHexForApply(" ", "Accent"));
    assertEquals("#AABBCC", SettingsColorSupport.normalizeOptionalHexForApply("#abc", "Accent"));

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> SettingsColorSupport.normalizeOptionalHexForApply("not-a-color", "Accent"));
    assertEquals(
        "Accent must be a hex value like #RRGGBB (or blank for default).", ex.getMessage());
  }

  @Test
  void uiSettingsColorNormalizationStaysStrict() {
    assertEquals("#1A2B3C", UiSettings.normalizeHexOrDefault("0x1a2b3c", "#445566"));
    assertEquals("#445566", UiSettings.normalizeHexOrDefault("abc", "#445566"));
    assertEquals("#6AA2FF", UiSettings.normalizeHexOrDefault("invalid", "invalid"));
    assertEquals("#AABBCC", UiSettings.normalizeHexOrNull("#aabbcc"));
    assertNull(UiSettings.normalizeHexOrNull("#abc"));
  }
}
