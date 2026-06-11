package cafe.woden.ircclient.ui.settings.chat;

import cafe.woden.ircclient.config.api.ChatBehaviorRuntimeConfigPort;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextField;

record TypingTreeIndicatorStyleOption(String id, String label) {}

record MatrixUserListNameDisplayModeOption(String id, String label) {}

public final class ChatBehaviorControlsSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private ChatBehaviorControlsSupport() {}

  public static JCheckBox buildPresenceFoldsCheckbox(UiSettings current) {
    JCheckBox presenceFolds =
        new JCheckBox(MESSAGES.text("preferences.chat.behavior.presenceFolds"));
    presenceFolds.setSelected(current.presenceFoldsEnabled());
    presenceFolds.setToolTipText(MESSAGES.text("preferences.chat.behavior.presenceFolds.tooltip"));
    return presenceFolds;
  }

  public static JCheckBox buildCtcpRequestsInActiveTargetCheckbox(UiSettings current) {
    JCheckBox ctcp = new JCheckBox(MESSAGES.text("preferences.chat.behavior.ctcpActiveTarget"));
    ctcp.setSelected(current.ctcpRequestsInActiveTargetEnabled());
    ctcp.setToolTipText(MESSAGES.text("preferences.chat.behavior.ctcpActiveTarget.tooltip"));
    return ctcp;
  }

  public static JTextField buildDefaultQuitMessageField(
      ChatBehaviorRuntimeConfigPort runtimeConfig) {
    JTextField field =
        new JTextField(runtimeConfig != null ? runtimeConfig.readDefaultQuitMessage() : "");
    field.setToolTipText(MESSAGES.text("preferences.chat.behavior.defaultQuitMessage.tooltip"));
    return field;
  }

  public static JCheckBox buildNickCompletionCycleWithTabCheckbox(boolean cycleWithTabEnabled) {
    JCheckBox checkbox =
        new JCheckBox(MESSAGES.text("preferences.chat.behavior.nickCompletion.cycleWithTab"));
    checkbox.setSelected(cycleWithTabEnabled);
    checkbox.setToolTipText(
        MESSAGES.text("preferences.chat.behavior.nickCompletion.cycleWithTab.tooltip"));
    return checkbox;
  }

  public static JCheckBox buildNickCompletionAppendAddressSuffixCheckbox(
      boolean appendAddressSuffixEnabled) {
    JCheckBox checkbox =
        new JCheckBox(
            MESSAGES.text("preferences.chat.behavior.nickCompletion.appendAddressSuffix"));
    checkbox.setSelected(appendAddressSuffixEnabled);
    checkbox.setToolTipText(
        MESSAGES.text("preferences.chat.behavior.nickCompletion.appendAddressSuffix.tooltip"));
    return checkbox;
  }

  public static JCheckBox buildOutgoingDeliveryIndicatorsCheckbox(UiSettings current) {
    JCheckBox checkbox =
        new JCheckBox(MESSAGES.text("preferences.chat.behavior.outgoingDeliveryIndicators"));
    checkbox.setSelected(current.outgoingDeliveryIndicatorsEnabled());
    checkbox.setToolTipText(
        MESSAGES.text("preferences.chat.behavior.outgoingDeliveryIndicators.tooltip"));
    return checkbox;
  }

  public static JCheckBox buildTypingIndicatorsSendCheckbox(UiSettings current) {
    JCheckBox checkbox = new JCheckBox(MESSAGES.text("preferences.ircv3.typing.send"));
    checkbox.setSelected(current.typingIndicatorsEnabled());
    checkbox.setToolTipText(MESSAGES.text("preferences.ircv3.typing.send.tooltip"));
    return checkbox;
  }

  public static JCheckBox buildTypingIndicatorsReceiveCheckbox(UiSettings current) {
    JCheckBox checkbox = new JCheckBox(MESSAGES.text("preferences.ircv3.typing.receive"));
    checkbox.setSelected(current.typingIndicatorsReceiveEnabled());
    checkbox.setToolTipText(MESSAGES.text("preferences.ircv3.typing.receive.tooltip"));
    return checkbox;
  }

  public static JCheckBox buildTypingIndicatorsTreeDisplayCheckbox(UiSettings current) {
    JCheckBox checkbox = new JCheckBox(MESSAGES.text("preferences.ircv3.typing.treeDisplay"));
    checkbox.setSelected(current.typingIndicatorsTreeEnabled());
    checkbox.setToolTipText(MESSAGES.text("preferences.ircv3.typing.treeDisplay.tooltip"));
    return checkbox;
  }

  public static JCheckBox buildTypingIndicatorsUsersListDisplayCheckbox(UiSettings current) {
    JCheckBox checkbox = new JCheckBox(MESSAGES.text("preferences.ircv3.typing.usersListDisplay"));
    checkbox.setSelected(current.typingIndicatorsUsersListEnabled());
    checkbox.setToolTipText(MESSAGES.text("preferences.ircv3.typing.usersListDisplay.tooltip"));
    return checkbox;
  }

  public static JCheckBox buildTypingIndicatorsTranscriptDisplayCheckbox(UiSettings current) {
    JCheckBox checkbox = new JCheckBox(MESSAGES.text("preferences.ircv3.typing.transcriptDisplay"));
    checkbox.setSelected(current.typingIndicatorsTranscriptEnabled());
    checkbox.setToolTipText(MESSAGES.text("preferences.ircv3.typing.transcriptDisplay.tooltip"));
    return checkbox;
  }

  public static JCheckBox buildTypingIndicatorsSendSignalDisplayCheckbox(UiSettings current) {
    JCheckBox checkbox = new JCheckBox(MESSAGES.text("preferences.ircv3.typing.sendSignalDisplay"));
    checkbox.setSelected(current.typingIndicatorsSendSignalEnabled());
    checkbox.setToolTipText(MESSAGES.text("preferences.ircv3.typing.sendSignalDisplay.tooltip"));
    return checkbox;
  }

  public static JComboBox<?> buildTypingTreeIndicatorStyleCombo(UiSettings current) {
    TypingTreeIndicatorStyleOption[] options =
        new TypingTreeIndicatorStyleOption[] {
          new TypingTreeIndicatorStyleOption(
              "dots", MESSAGES.text("preferences.ircv3.typing.treeStyle.dots")),
          new TypingTreeIndicatorStyleOption(
              "keyboard", MESSAGES.text("preferences.ircv3.typing.treeStyle.keyboard")),
          new TypingTreeIndicatorStyleOption(
              "glow-dot", MESSAGES.text("preferences.ircv3.typing.treeStyle.glowDot"))
        };
    JComboBox<TypingTreeIndicatorStyleOption> combo = new JComboBox<>(options);
    combo.setToolTipText(MESSAGES.text("preferences.ircv3.typing.treeStyle.tooltip"));
    combo.setRenderer(
        new DefaultListCellRenderer() {
          @Override
          public java.awt.Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label =
                (JLabel)
                    super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
            if (value instanceof TypingTreeIndicatorStyleOption option) {
              label.setText(option.label());
            }
            return label;
          }
        });

    String configured = current != null ? current.typingIndicatorsTreeStyle() : null;
    String normalized = UiSettings.normalizeTypingTreeIndicatorStyle(configured);
    for (TypingTreeIndicatorStyleOption option : options) {
      if (option.id().equalsIgnoreCase(normalized)) {
        combo.setSelectedItem(option);
        break;
      }
    }
    return combo;
  }

  public static JComboBox<?> buildMatrixUserListNameDisplayModeCombo(UiSettings current) {
    MatrixUserListNameDisplayModeOption[] options =
        new MatrixUserListNameDisplayModeOption[] {
          new MatrixUserListNameDisplayModeOption(
              "compact", MESSAGES.text("preferences.ircv3.matrixNames.compact")),
          new MatrixUserListNameDisplayModeOption(
              "verbose", MESSAGES.text("preferences.ircv3.matrixNames.verbose"))
        };
    JComboBox<MatrixUserListNameDisplayModeOption> combo = new JComboBox<>(options);
    combo.setToolTipText(MESSAGES.text("preferences.ircv3.matrixNames.tooltip"));
    combo.setRenderer(
        new DefaultListCellRenderer() {
          @Override
          public java.awt.Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label =
                (JLabel)
                    super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
            if (value instanceof MatrixUserListNameDisplayModeOption option) {
              label.setText(option.label());
            }
            return label;
          }
        });

    String configured = current != null ? current.matrixUserListNameDisplayMode() : null;
    String normalized = UiSettings.normalizeMatrixUserListNameDisplayMode(configured);
    for (MatrixUserListNameDisplayModeOption option : options) {
      if (option.id().equalsIgnoreCase(normalized)) {
        combo.setSelectedItem(option);
        break;
      }
    }
    return combo;
  }

  public static JCheckBox buildServerTreeNotificationBadgesCheckbox(UiSettings current) {
    JCheckBox checkbox = new JCheckBox(MESSAGES.text("preferences.ircv3.serverTreeBadges.enabled"));
    checkbox.setSelected(current.serverTreeNotificationBadgesEnabled());
    checkbox.setToolTipText(MESSAGES.text("preferences.ircv3.serverTreeBadges.tooltip"));
    return checkbox;
  }

  public static JSpinner buildServerTreeUnreadBadgeScalePercentSpinner(
      ChatBehaviorRuntimeConfigPort runtimeConfig) {
    int current =
        runtimeConfig != null ? runtimeConfig.readServerTreeUnreadBadgeScalePercent(100) : 100;
    JSpinner spinner =
        PreferencesUiSupport.numberSpinner(
            SettingsRangeSupport.normalizeServerTreeUnreadBadgeScalePercent(current), 50, 150, 5);
    spinner.setToolTipText(MESSAGES.text("preferences.ircv3.serverTreeBadgeScale.tooltip"));
    return spinner;
  }

  public static ChatBehaviorSettings readSettings(
      ChatBehaviorRuntimeConfigPort runtimeConfig,
      JCheckBox presenceFolds,
      JCheckBox ctcpRequestsInActiveTarget,
      JTextField defaultQuitMessage,
      JCheckBox nickCompletionCycleWithTab,
      JCheckBox nickCompletionAppendAddressSuffix,
      JCheckBox typingIndicatorsSendEnabled,
      JCheckBox typingIndicatorsReceiveEnabled,
      JComboBox<?> typingTreeIndicatorStyle,
      JCheckBox typingIndicatorsTreeDisplayEnabled,
      JCheckBox typingIndicatorsUsersListDisplayEnabled,
      JCheckBox typingIndicatorsTranscriptDisplayEnabled,
      JCheckBox typingIndicatorsSendSignalDisplayEnabled,
      JComboBox<?> matrixUserListNameDisplayMode,
      JCheckBox serverTreeNotificationBadgesEnabled,
      JSpinner serverTreeUnreadBadgeScalePercent) {
    String quitMessage = normalizeDefaultQuitMessage(runtimeConfig, defaultQuitMessage.getText());
    defaultQuitMessage.setText(quitMessage);

    return new ChatBehaviorSettings(
        presenceFolds.isSelected(),
        ctcpRequestsInActiveTarget.isSelected(),
        quitMessage,
        nickCompletionCycleWithTab.isSelected(),
        nickCompletionAppendAddressSuffix.isSelected(),
        typingIndicatorsSendEnabled.isSelected(),
        typingIndicatorsReceiveEnabled.isSelected(),
        typingTreeIndicatorStyleValue(typingTreeIndicatorStyle),
        typingIndicatorsTreeDisplayEnabled.isSelected(),
        typingIndicatorsUsersListDisplayEnabled.isSelected(),
        typingIndicatorsTranscriptDisplayEnabled.isSelected(),
        typingIndicatorsSendSignalDisplayEnabled.isSelected(),
        matrixUserListNameDisplayModeValue(matrixUserListNameDisplayMode),
        serverTreeNotificationBadgesEnabled.isSelected(),
        SettingsRangeSupport.normalizeServerTreeUnreadBadgeScalePercent(
            PreferencesUiSupport.spinnerInt(serverTreeUnreadBadgeScalePercent)));
  }

  public static void rememberServerTreeSettings(
      ChatBehaviorRuntimeConfigPort runtimeConfig, ChatBehaviorSettings settings) {
    runtimeConfig.rememberServerTreeUnreadBadgeScalePercent(
        settings.serverTreeUnreadBadgeScalePercent());
    runtimeConfig.rememberServerTreeNotificationBadgesEnabled(
        settings.serverTreeNotificationBadgesEnabled());
  }

  public static void rememberSettings(
      ChatBehaviorRuntimeConfigPort runtimeConfig, ChatBehaviorSettings settings) {
    runtimeConfig.rememberPresenceFoldsEnabled(settings.presenceFoldsEnabled());
    runtimeConfig.rememberCtcpRequestsInActiveTargetEnabled(
        settings.ctcpRequestsInActiveTargetEnabled());
    runtimeConfig.rememberDefaultQuitMessage(settings.defaultQuitMessage());
    runtimeConfig.rememberNickCompletionCycleWithTabEnabled(
        settings.nickCompletionCycleWithTabEnabled());
    runtimeConfig.rememberNickCompletionAppendAddressSuffixEnabled(
        settings.nickCompletionAppendAddressSuffixEnabled());
    runtimeConfig.rememberTypingIndicatorsEnabled(settings.typingIndicatorsSendEnabled());
    runtimeConfig.rememberTypingIndicatorsReceiveEnabled(settings.typingIndicatorsReceiveEnabled());
    runtimeConfig.rememberTypingTreeIndicatorStyle(settings.typingIndicatorsTreeStyle());
    runtimeConfig.rememberTypingIndicatorsTreeEnabled(
        settings.typingIndicatorsTreeDisplayEnabled());
    runtimeConfig.rememberTypingIndicatorsUsersListEnabled(
        settings.typingIndicatorsUsersListDisplayEnabled());
    runtimeConfig.rememberMatrixUserListNameDisplayMode(settings.matrixUserListNameDisplayMode());
    runtimeConfig.rememberTypingIndicatorsTranscriptEnabled(
        settings.typingIndicatorsTranscriptDisplayEnabled());
    runtimeConfig.rememberTypingIndicatorsSendSignalEnabled(
        settings.typingIndicatorsSendSignalDisplayEnabled());
  }

  static String typingTreeIndicatorStyleValue(JComboBox<?> combo) {
    Object selected = combo != null ? combo.getSelectedItem() : null;
    if (selected instanceof TypingTreeIndicatorStyleOption option) {
      return UiSettings.normalizeTypingTreeIndicatorStyle(option.id());
    }
    return "dots";
  }

  static String matrixUserListNameDisplayModeValue(JComboBox<?> combo) {
    Object selected = combo != null ? combo.getSelectedItem() : null;
    if (selected instanceof MatrixUserListNameDisplayModeOption option) {
      return UiSettings.normalizeMatrixUserListNameDisplayMode(option.id());
    }
    return "compact";
  }

  private static String normalizeDefaultQuitMessage(
      ChatBehaviorRuntimeConfigPort runtimeConfig, String raw) {
    if (runtimeConfig != null) {
      return runtimeConfig.normalizeDefaultQuitMessage(raw);
    }
    String message =
        java.util.Objects.toString(raw, "").replace('\r', ' ').replace('\n', ' ').trim();
    return message.isEmpty() ? ChatBehaviorRuntimeConfigPort.DEFAULT_QUIT_MESSAGE : message;
  }

  public record ChatBehaviorSettings(
      boolean presenceFoldsEnabled,
      boolean ctcpRequestsInActiveTargetEnabled,
      String defaultQuitMessage,
      boolean nickCompletionCycleWithTabEnabled,
      boolean nickCompletionAppendAddressSuffixEnabled,
      boolean typingIndicatorsSendEnabled,
      boolean typingIndicatorsReceiveEnabled,
      String typingIndicatorsTreeStyle,
      boolean typingIndicatorsTreeDisplayEnabled,
      boolean typingIndicatorsUsersListDisplayEnabled,
      boolean typingIndicatorsTranscriptDisplayEnabled,
      boolean typingIndicatorsSendSignalDisplayEnabled,
      String matrixUserListNameDisplayMode,
      boolean serverTreeNotificationBadgesEnabled,
      int serverTreeUnreadBadgeScalePercent) {}
}
