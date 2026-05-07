package cafe.woden.ircclient.ui.settings;

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

class ChatDisplayControlsSupportTest {

  @Test
  void readTimestampSettingsDefaultsBlankFormatAndUpdatesField() {
    TimestampControls controls = timestampControls(true, "   ", true, false);

    ChatDisplayControlsSupport.TimestampSettings settings =
        ChatDisplayControlsSupport.readTimestampSettings(controls);

    assertTrue(settings.enabled());
    assertEquals("HH:mm:ss", settings.format());
    assertEquals("HH:mm:ss", controls.format.getText());
    assertTrue(settings.includeChatMessages());
  }

  @Test
  void readTimestampSettingsRejectsInvalidDateTimePattern() {
    TimestampControls controls = timestampControls(true, "HH:mm:ss 'unterminated", true, true);

    ChatDisplayControlsSupport.TimestampSettingsException ex =
        assertThrows(
            ChatDisplayControlsSupport.TimestampSettingsException.class,
            () -> ChatDisplayControlsSupport.readTimestampSettings(controls));

    assertEquals("Invalid timestamp format", ex.title());
    assertTrue(ex.getMessage().contains("Invalid timestamp format: HH:mm:ss 'unterminated"));
  }

  @Test
  void rememberTimestampSettingsPersistsTimestampValues() {
    RuntimeConfigStore runtimeConfig = mock(RuntimeConfigStore.class);
    ChatDisplayControlsSupport.TimestampSettings settings =
        new ChatDisplayControlsSupport.TimestampSettings(true, "HH:mm", false, true);

    ChatDisplayControlsSupport.rememberTimestampSettings(runtimeConfig, settings);

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
