package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.JSpinner;
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

  private static JSpinner spinner(int value) {
    return PreferencesUiSupport.numberSpinner(value, 0, 200, 1);
  }
}
