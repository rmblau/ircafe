package cafe.woden.ircclient.ui.settings.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.api.ChatBehaviorRuntimeConfigPort;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class ChatBehaviorControlsSupportTest {

  @Test
  void readSettingsNormalizesQuitMessageAndClampsServerTreeBadgeScale() {
    JTextField defaultQuitMessage = new JTextField(" Bye\r\nnow ");
    JComboBox<TypingTreeIndicatorStyleOption> typingStyle =
        new JComboBox<>(
            new TypingTreeIndicatorStyleOption[] {
              new TypingTreeIndicatorStyleOption("keyboard", "Keyboard")
            });
    JComboBox<MatrixUserListNameDisplayModeOption> matrixMode =
        new JComboBox<>(
            new MatrixUserListNameDisplayModeOption[] {
              new MatrixUserListNameDisplayModeOption("verbose", "Verbose")
            });

    ChatBehaviorControlsSupport.ChatBehaviorSettings settings =
        ChatBehaviorControlsSupport.readSettings(
            null,
            selected(true),
            selected(false),
            defaultQuitMessage,
            selected(true),
            selected(false),
            selected(true),
            selected(false),
            typingStyle,
            selected(true),
            selected(false),
            selected(true),
            selected(false),
            matrixMode,
            selected(true),
            spinner(999));

    assertTrue(settings.presenceFoldsEnabled());
    assertFalse(settings.ctcpRequestsInActiveTargetEnabled());
    assertEquals("Bye  now", settings.defaultQuitMessage());
    assertEquals("Bye  now", defaultQuitMessage.getText());
    assertTrue(settings.nickCompletionCycleWithTabEnabled());
    assertFalse(settings.nickCompletionAppendAddressSuffixEnabled());
    assertTrue(settings.typingIndicatorsSendEnabled());
    assertFalse(settings.typingIndicatorsReceiveEnabled());
    assertEquals("keyboard", settings.typingIndicatorsTreeStyle());
    assertTrue(settings.typingIndicatorsTreeDisplayEnabled());
    assertFalse(settings.typingIndicatorsUsersListDisplayEnabled());
    assertTrue(settings.typingIndicatorsTranscriptDisplayEnabled());
    assertFalse(settings.typingIndicatorsSendSignalDisplayEnabled());
    assertEquals("verbose", settings.matrixUserListNameDisplayMode());
    assertTrue(settings.serverTreeNotificationBadgesEnabled());
    assertEquals(150, settings.serverTreeUnreadBadgeScalePercent());
  }

  @Test
  void readSettingsDefaultsBlankQuitMessage() {
    JTextField defaultQuitMessage = new JTextField("   ");

    ChatBehaviorControlsSupport.ChatBehaviorSettings settings =
        ChatBehaviorControlsSupport.readSettings(
            null,
            selected(false),
            selected(false),
            defaultQuitMessage,
            selected(false),
            selected(true),
            selected(false),
            selected(false),
            new JComboBox<>(),
            selected(false),
            selected(false),
            selected(false),
            selected(false),
            new JComboBox<>(),
            selected(false),
            spinner(25));

    assertEquals(ChatBehaviorRuntimeConfigPort.DEFAULT_QUIT_MESSAGE, settings.defaultQuitMessage());
    assertEquals(ChatBehaviorRuntimeConfigPort.DEFAULT_QUIT_MESSAGE, defaultQuitMessage.getText());
    assertEquals("dots", settings.typingIndicatorsTreeStyle());
    assertEquals("compact", settings.matrixUserListNameDisplayMode());
    assertEquals(50, settings.serverTreeUnreadBadgeScalePercent());
  }

  @Test
  void rememberSettingsPersistsChatBehaviorSettings() {
    ChatBehaviorRuntimeConfigPort runtimeConfig = mock(ChatBehaviorRuntimeConfigPort.class);
    ChatBehaviorControlsSupport.ChatBehaviorSettings settings =
        new ChatBehaviorControlsSupport.ChatBehaviorSettings(
            true,
            false,
            "Bye",
            true,
            false,
            true,
            false,
            "glow-dot",
            true,
            false,
            true,
            false,
            "verbose",
            true,
            125);

    ChatBehaviorControlsSupport.rememberServerTreeSettings(runtimeConfig, settings);
    ChatBehaviorControlsSupport.rememberSettings(runtimeConfig, settings);

    verify(runtimeConfig).rememberServerTreeUnreadBadgeScalePercent(125);
    verify(runtimeConfig).rememberServerTreeNotificationBadgesEnabled(true);
    verify(runtimeConfig).rememberPresenceFoldsEnabled(true);
    verify(runtimeConfig).rememberCtcpRequestsInActiveTargetEnabled(false);
    verify(runtimeConfig).rememberDefaultQuitMessage("Bye");
    verify(runtimeConfig).rememberNickCompletionCycleWithTabEnabled(true);
    verify(runtimeConfig).rememberNickCompletionAppendAddressSuffixEnabled(false);
    verify(runtimeConfig).rememberTypingIndicatorsEnabled(true);
    verify(runtimeConfig).rememberTypingIndicatorsReceiveEnabled(false);
    verify(runtimeConfig).rememberTypingTreeIndicatorStyle("glow-dot");
    verify(runtimeConfig).rememberTypingIndicatorsTreeEnabled(true);
    verify(runtimeConfig).rememberTypingIndicatorsUsersListEnabled(false);
    verify(runtimeConfig).rememberMatrixUserListNameDisplayMode("verbose");
    verify(runtimeConfig).rememberTypingIndicatorsTranscriptEnabled(true);
    verify(runtimeConfig).rememberTypingIndicatorsSendSignalEnabled(false);
  }

  private static JCheckBox selected(boolean selected) {
    JCheckBox checkbox = new JCheckBox();
    checkbox.setSelected(selected);
    return checkbox;
  }

  private static JSpinner spinner(int value) {
    return new JSpinner(new SpinnerNumberModel(value, -1_000, 1_000, 1));
  }
}
