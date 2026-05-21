package cafe.woden.ircclient.ui.settings.timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class TimestampControlsSupportTest {

  @Test
  void readSettingsDefaultsBlankFormatAndUpdatesField() {
    TimestampControls controls = timestampControls(true, "   ", true, false);

    TimestampControlsSupport.TimestampSettings settings =
        TimestampControlsSupport.readSettings(controls);

    assertTrue(settings.enabled());
    assertEquals("HH:mm:ss", settings.format());
    assertEquals("HH:mm:ss", controls.format.getText());
    assertTrue(settings.includeChatMessages());
  }

  @Test
  void readSettingsRejectsInvalidDateTimePattern() {
    TimestampControls controls = timestampControls(true, "HH:mm:ss 'unterminated", true, true);

    TimestampControlsSupport.TimestampSettingsException ex =
        assertThrows(
            TimestampControlsSupport.TimestampSettingsException.class,
            () -> TimestampControlsSupport.readSettings(controls));

    assertEquals("Invalid timestamp format", ex.title());
    assertTrue(ex.getMessage().contains("Invalid timestamp format: HH:mm:ss 'unterminated"));
  }

  @Test
  void rememberSettingsPersistsTimestampValues() {
    RuntimeConfigStore runtimeConfig = mock(RuntimeConfigStore.class);
    TimestampControlsSupport.TimestampSettings settings =
        new TimestampControlsSupport.TimestampSettings(true, "HH:mm", false, true);

    TimestampControlsSupport.rememberSettings(runtimeConfig, settings);

    verify(runtimeConfig).rememberTimestampsEnabled(true);
    verify(runtimeConfig).rememberTimestampFormat("HH:mm");
    verify(runtimeConfig).rememberTimestampsIncludeChatMessages(false);
    verify(runtimeConfig).rememberTimestampsIncludePresenceMessages(true);
  }

  private static TimestampControls timestampControls(
      boolean enabled,
      String format,
      boolean includeChatMessages,
      boolean includePresenceMessages) {
    JCheckBox enabledBox = new JCheckBox();
    enabledBox.setSelected(enabled);
    JCheckBox includeChat = new JCheckBox();
    includeChat.setSelected(includeChatMessages);
    JCheckBox includePresence = new JCheckBox();
    includePresence.setSelected(includePresenceMessages);
    return new TimestampControls(
        enabledBox, new JTextField(format), includeChat, includePresence, new JPanel());
  }
}
