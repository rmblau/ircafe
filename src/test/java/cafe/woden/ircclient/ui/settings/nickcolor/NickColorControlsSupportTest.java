package cafe.woden.ircclient.ui.settings.nickcolor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.api.NickColorRuntimeConfigPort;
import cafe.woden.ircclient.ui.chat.NickColorSettings;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import org.junit.jupiter.api.Test;

class NickColorControlsSupportTest {

  @Test
  void readSettingsBuildsNickColorSettingsFromControls() {
    JCheckBox enabled = new JCheckBox();
    enabled.setSelected(false);
    NickColorControls controls =
        new NickColorControls(enabled, spinner(-1.0), new JButton("Overrides"), new JPanel());

    NickColorSettings settings = NickColorControlsSupport.readSettings(controls);

    assertFalse(settings.enabled());
    assertEquals(3.0, settings.minContrast());
  }

  @Test
  void rememberSettingsUpdatesBusAndPersistsRuntimeConfig() {
    NickColorRuntimeConfigPort runtimeConfig = mock(NickColorRuntimeConfigPort.class);
    NickColorSettingsBus nickColorSettingsBus = mock(NickColorSettingsBus.class);
    NickColorSettings settings = new NickColorSettings(true, 4.5);

    NickColorControlsSupport.rememberSettings(runtimeConfig, nickColorSettingsBus, settings);

    verify(nickColorSettingsBus).set(settings);
    verify(runtimeConfig).rememberNickColoringEnabled(true);
    verify(runtimeConfig).rememberNickColorMinContrast(4.5);
  }

  private static JSpinner spinner(double value) {
    return PreferencesUiSupport.numberSpinner(value, -21.0, 21.0, 0.5);
  }
}
