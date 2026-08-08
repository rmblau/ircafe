package cafe.woden.ircclient.ui.settings.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.api.MemoryUsageRuntimeConfigPort;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class MemoryControlsSupportTest {

  @Test
  void readSettingsNormalizesMemoryValues() {
    JComboBox<MemoryUsageDisplayMode> displayMode =
        new JComboBox<>(MemoryUsageDisplayMode.values());
    displayMode.setSelectedItem(MemoryUsageDisplayMode.SHORT);
    MemoryWarningControls warnings =
        new MemoryWarningControls(
            spinner(99), selected(true), selected(false), selected(true), selected(false));

    MemoryControlsSupport.MemorySettings settings =
        MemoryControlsSupport.readSettings(displayMode, spinner(100), warnings);

    assertEquals(MemoryUsageDisplayMode.SHORT, settings.displayMode());
    assertEquals(250, settings.refreshIntervalMs());
    assertEquals(50, settings.warningNearMaxPercent());
    assertTrue(settings.warningTooltipEnabled());
    assertFalse(settings.warningToastEnabled());
    assertTrue(settings.warningPushyEnabled());
    assertFalse(settings.warningSoundEnabled());
  }

  @Test
  void rememberSettingsPersistsMemoryValues() {
    MemoryUsageRuntimeConfigPort runtimeConfig = mock(MemoryUsageRuntimeConfigPort.class);
    MemoryControlsSupport.MemorySettings settings =
        new MemoryControlsSupport.MemorySettings(
            MemoryUsageDisplayMode.LONG, 2000, 7, true, false, true, false);

    MemoryControlsSupport.rememberSettings(runtimeConfig, settings);

    verify(runtimeConfig).rememberMemoryUsageDisplayMode(MemoryUsageDisplayMode.LONG.token());
    verify(runtimeConfig).rememberMemoryUsageRefreshIntervalMs(2000);
    verify(runtimeConfig).rememberMemoryUsageWarningNearMaxPercent(7);
    verify(runtimeConfig).rememberMemoryUsageWarningTooltipEnabled(true);
    verify(runtimeConfig).rememberMemoryUsageWarningToastEnabled(false);
    verify(runtimeConfig).rememberMemoryUsageWarningPushyEnabled(true);
    verify(runtimeConfig).rememberMemoryUsageWarningSoundEnabled(false);
  }

  private static JCheckBox selected(boolean selected) {
    JCheckBox checkbox = new JCheckBox();
    checkbox.setSelected(selected);
    return checkbox;
  }

  private static JSpinner spinner(int value) {
    return new JSpinner(new SpinnerNumberModel(value, -100_000, 100_000, 1));
  }
}
