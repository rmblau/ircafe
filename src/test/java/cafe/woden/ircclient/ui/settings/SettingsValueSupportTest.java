package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class SettingsValueSupportTest {

  @Test
  void trimsPlainValues() {
    assertEquals("value", SettingsValueSupport.trimmedString(" value "));
    assertEquals("", SettingsValueSupport.trimmedString(null));
    assertEquals("42", SettingsValueSupport.trimmedString(42));
  }

  @Test
  void trimsBlankPlainValuesToNull() {
    assertEquals("value", SettingsValueSupport.trimmedStringOrNull(" value "));
    assertNull(SettingsValueSupport.trimmedStringOrNull("   "));
    assertNull(SettingsValueSupport.trimmedStringOrNull(null));
  }

  @Test
  void lowercasesTrimmedPlainValuesWithRootLocale() {
    assertEquals("value", SettingsValueSupport.lowerTrimmedString(" VALUE "));
    assertEquals("", SettingsValueSupport.lowerTrimmedString(null));
  }

  @Test
  void trimsNonBlankLines() {
    assertEquals(List.of("-d 1000", "-c"), SettingsValueSupport.trimmedLines(" -d 1000 \n\n -c "));
    assertEquals(List.of(), SettingsValueSupport.trimmedLines(" \n\t "));
    assertEquals(List.of(), SettingsValueSupport.trimmedLines(null));
  }

  @Test
  void clampsPlainIntValue() {
    assertEquals(50, SettingsValueSupport.clampInt(25, 50, 150));
    assertEquals(125, SettingsValueSupport.clampInt(125, 50, 150));
    assertEquals(150, SettingsValueSupport.clampInt(175, 50, 150));
  }
}
