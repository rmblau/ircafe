package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class PreferencesUiSupportTest {

  @Test
  void readsSpinnerIntValue() {
    JSpinner spinner = PreferencesUiSupport.numberSpinner(42, 0, 100, 1);

    assertEquals(42, PreferencesUiSupport.spinnerInt(spinner));
  }

  @Test
  void readsSpinnerDoubleValue() {
    JSpinner spinner = PreferencesUiSupport.numberSpinner(3.5, 0.0, 10.0, 0.5);

    assertEquals(3.5, PreferencesUiSupport.spinnerDouble(spinner));
  }

  @Test
  void clampsSpinnerIntValue() {
    assertEquals(50, PreferencesUiSupport.clampedSpinnerInt(spinner(25), 50, 150));
    assertEquals(125, PreferencesUiSupport.clampedSpinnerInt(spinner(125), 50, 150));
    assertEquals(150, PreferencesUiSupport.clampedSpinnerInt(spinner(175), 50, 150));
  }

  @Test
  void clampsPlainIntValue() {
    assertEquals(50, PreferencesUiSupport.clampInt(25, 50, 150));
    assertEquals(125, PreferencesUiSupport.clampInt(125, 50, 150));
    assertEquals(150, PreferencesUiSupport.clampInt(175, 50, 150));
  }

  @Test
  void trimsTextComponentValues() {
    assertEquals("value", PreferencesUiSupport.trimmedText(new JTextField(" value ")));
    assertEquals("", PreferencesUiSupport.trimmedText(null));
  }

  @Test
  void trimsBlankTextComponentValuesToNull() {
    assertEquals("value", PreferencesUiSupport.trimmedTextOrNull(new JTextField(" value ")));
    assertNull(PreferencesUiSupport.trimmedTextOrNull(new JTextField("   ")));
    assertNull(PreferencesUiSupport.trimmedTextOrNull(null));
  }

  @Test
  void trimsPlainValues() {
    assertEquals("value", PreferencesUiSupport.trimmedString(" value "));
    assertEquals("", PreferencesUiSupport.trimmedString(null));
    assertEquals("42", PreferencesUiSupport.trimmedString(42));
  }

  @Test
  void trimsBlankPlainValuesToNull() {
    assertEquals("value", PreferencesUiSupport.trimmedStringOrNull(" value "));
    assertNull(PreferencesUiSupport.trimmedStringOrNull("   "));
    assertNull(PreferencesUiSupport.trimmedStringOrNull(null));
  }

  @Test
  void trimsNonBlankLines() {
    assertEquals(List.of("-d 1000", "-c"), PreferencesUiSupport.trimmedLines(" -d 1000 \n\n -c "));
    assertEquals(List.of(), PreferencesUiSupport.trimmedLines(" \n\t "));
    assertEquals(List.of(), PreferencesUiSupport.trimmedLines(null));
  }

  @Test
  void truncatesTrimmedPlainValues() {
    assertEquals("hello", PreferencesUiSupport.truncateText(" hello ", 12));
    assertEquals("hello", PreferencesUiSupport.truncateText(" hello ", 5));
    assertEquals("hel\u2026", PreferencesUiSupport.truncateText(" hello ", 4));
    assertEquals("\u2026", PreferencesUiSupport.truncateText("hello", 0));
  }

  @Test
  void readsPasswordFieldValues() {
    assertEquals(" value ", PreferencesUiSupport.passwordText(new JPasswordField(" value ")));
    assertEquals("", PreferencesUiSupport.passwordText(null));
  }

  @Test
  void trimsPasswordFieldValues() {
    assertEquals("value", PreferencesUiSupport.trimmedPasswordText(new JPasswordField(" value ")));
    assertEquals("", PreferencesUiSupport.trimmedPasswordText(null));
  }

  @Test
  void readsTypedComboSelectionOrFallback() {
    JComboBox<Object> combo = new JComboBox<>(new Object[] {"first", 42});

    assertEquals("first", PreferencesUiSupport.selectedComboItem(combo, String.class, "fallback"));

    combo.setSelectedItem(42);
    assertEquals(
        "fallback", PreferencesUiSupport.selectedComboItem(combo, String.class, "fallback"));
    assertEquals(42, PreferencesUiSupport.selectedComboItem(combo, Integer.class, -1));
  }

  @Test
  void fallsBackForMissingComboSelectionInputs() {
    assertEquals(
        "fallback", PreferencesUiSupport.selectedComboItem(null, String.class, "fallback"));

    JComboBox<Object> combo = new JComboBox<>(new Object[] {"first"});
    assertEquals("fallback", PreferencesUiSupport.selectedComboItem(combo, null, "fallback"));
  }

  @Test
  void readsTrimmedComboSelectionText() {
    JComboBox<Object> combo = new JComboBox<>(new Object[] {" value "});

    assertEquals("value", PreferencesUiSupport.selectedComboText(combo));
    assertEquals("", PreferencesUiSupport.selectedComboText(null));
  }

  private static JSpinner spinner(int value) {
    return PreferencesUiSupport.numberSpinner(value, 0, 200, 1);
  }
}
