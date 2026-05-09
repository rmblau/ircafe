package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class AppearanceControlsSupportTest {

  @Test
  void readServerTreeSettingsNormalizesOptionalColors() throws Exception {
    AppearanceServerTreeControls controls = serverTreeControls(" abc ", "0x102030", true);

    AppearanceControlsSupport.ServerTreeAppearanceSettings settings =
        AppearanceControlsSupport.readServerTreeSettings(controls);

    assertEquals("#AABBCC", settings.unreadChannelColor());
    assertEquals("#102030", settings.highlightChannelColor());
    assertTrue(settings.preserveDockLayoutBetweenSessions());
  }

  @Test
  void readServerTreeSettingsKeepsBlankColorsAsNull() throws Exception {
    AppearanceServerTreeControls controls = serverTreeControls(" ", "", false);

    AppearanceControlsSupport.ServerTreeAppearanceSettings settings =
        AppearanceControlsSupport.readServerTreeSettings(controls);

    assertNull(settings.unreadChannelColor());
    assertNull(settings.highlightChannelColor());
  }

  @Test
  void readServerTreeSettingsReportsInvalidColorWithDialogTitle() {
    AppearanceServerTreeControls controls = serverTreeControls("not-a-color", "", false);

    AppearanceControlsSupport.ServerTreeAppearanceSettingsException ex =
        assertThrows(
            AppearanceControlsSupport.ServerTreeAppearanceSettingsException.class,
            () -> AppearanceControlsSupport.readServerTreeSettings(controls));

    assertEquals("Invalid server tree color", ex.title());
    assertTrue(ex.getMessage().contains("Unread channel color"));
  }

  @Test
  void rememberServerTreeSettingsPersistsAppearanceValues() {
    RuntimeConfigStore runtimeConfig = mock(RuntimeConfigStore.class);
    AppearanceControlsSupport.ServerTreeAppearanceSettings settings =
        new AppearanceControlsSupport.ServerTreeAppearanceSettings("#112233", "#445566", true);

    AppearanceControlsSupport.rememberServerTreeSettings(runtimeConfig, settings);

    verify(runtimeConfig).rememberServerTreeUnreadChannelColor("#112233");
    verify(runtimeConfig).rememberServerTreeHighlightChannelColor("#445566");
    verify(runtimeConfig).rememberPreserveDockLayout(true);
  }

  private static AppearanceServerTreeControls serverTreeControls(
      String unreadColor, String highlightColor, boolean preserveLayout) {
    JCheckBox preserve = new JCheckBox();
    preserve.setSelected(preserveLayout);
    return new AppearanceServerTreeControls(
        colorField(unreadColor), colorField(highlightColor), preserve);
  }

  private static ColorField colorField(String hex) {
    return new ColorField(
        new JTextField(hex), new JButton(), new JButton(), new JPanel(), () -> {});
  }
}
