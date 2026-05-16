package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
  void clampsSpinnerIntValue() {
    assertEquals(50, PreferencesUiSupport.clampedSpinnerInt(spinner(25), 50, 150));
    assertEquals(125, PreferencesUiSupport.clampedSpinnerInt(spinner(125), 50, 150));
    assertEquals(150, PreferencesUiSupport.clampedSpinnerInt(spinner(175), 50, 150));
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
  void readsPasswordFieldValues() {
    assertEquals(" value ", PreferencesUiSupport.passwordText(new JPasswordField(" value ")));
    assertEquals("", PreferencesUiSupport.passwordText(null));
  }

  @Test
  void trimsPasswordFieldValues() {
    assertEquals("value", PreferencesUiSupport.trimmedPasswordText(new JPasswordField(" value ")));
    assertEquals("", PreferencesUiSupport.trimmedPasswordText(null));
  }

  private static JSpinner spinner(int value) {
    return PreferencesUiSupport.numberSpinner(value, 0, 200, 1);
  }
}
