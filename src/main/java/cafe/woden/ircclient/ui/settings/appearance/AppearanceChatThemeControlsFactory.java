package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettings;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSlider;

final class AppearanceChatThemeControlsFactory {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private AppearanceChatThemeControlsFactory() {}

  static ChatThemeControls build(ChatThemeSettings current) {
    JComboBox<ChatThemeSettings.Preset> preset = createPresetCombo(current);
    ColorField timestamp =
        AppearanceColorFieldFactory.build(
            current != null ? current.timestampColor() : null,
            MESSAGES.text("preferences.appearance.chatTheme.picker.timestamp"));
    ColorField system =
        AppearanceColorFieldFactory.build(
            current != null ? current.systemColor() : null,
            MESSAGES.text("preferences.appearance.chatTheme.picker.system"));
    ColorField mention =
        AppearanceColorFieldFactory.build(
            current != null ? current.mentionBgColor() : null,
            MESSAGES.text("preferences.appearance.chatTheme.picker.mention"));
    ColorField message =
        AppearanceColorFieldFactory.build(
            current != null ? current.messageColor() : null,
            MESSAGES.text("preferences.appearance.chatTheme.picker.message"));
    ColorField notice =
        AppearanceColorFieldFactory.build(
            current != null ? current.noticeColor() : null,
            MESSAGES.text("preferences.appearance.chatTheme.picker.notice"));
    ColorField action =
        AppearanceColorFieldFactory.build(
            current != null ? current.actionColor() : null,
            MESSAGES.text("preferences.appearance.chatTheme.picker.action"));
    ColorField error =
        AppearanceColorFieldFactory.build(
            current != null ? current.errorColor() : null,
            MESSAGES.text("preferences.appearance.chatTheme.picker.error"));
    ColorField presence =
        AppearanceColorFieldFactory.build(
            current != null ? current.presenceColor() : null,
            MESSAGES.text("preferences.appearance.chatTheme.picker.presence"));

    return new ChatThemeControls(
        preset,
        timestamp,
        system,
        mention,
        message,
        notice,
        action,
        error,
        presence,
        createMentionStrengthSlider(current));
  }

  private static JComboBox<ChatThemeSettings.Preset> createPresetCombo(ChatThemeSettings current) {
    JComboBox<ChatThemeSettings.Preset> preset = new JComboBox<>(ChatThemeSettings.Preset.values());
    preset.setRenderer(new ChatThemePresetRenderer());
    preset.setSelectedItem(current != null ? current.preset() : ChatThemeSettings.Preset.DEFAULT);
    return preset;
  }

  private static JSlider createMentionStrengthSlider(ChatThemeSettings current) {
    int strength = current != null ? current.mentionStrength() : 35;
    JSlider mentionStrength =
        new JSlider(0, 100, SettingsRangeSupport.normalizeThemePercent(strength));
    mentionStrength.setMajorTickSpacing(25);
    mentionStrength.setMinorTickSpacing(5);
    mentionStrength.setPaintTicks(false);
    mentionStrength.setPaintLabels(false);
    mentionStrength.setToolTipText(
        MESSAGES.text("preferences.appearance.chatTheme.mentionStrength.tooltip"));
    return mentionStrength;
  }

  private static String labelFor(ChatThemeSettings.Preset presetValue) {
    return switch (presetValue) {
      case DEFAULT -> MESSAGES.text("preferences.appearance.chatTheme.preset.default");
      case SOFT -> MESSAGES.text("preferences.appearance.chatTheme.preset.soft");
      case ACCENTED -> MESSAGES.text("preferences.appearance.chatTheme.preset.accented");
      case HIGH_CONTRAST -> MESSAGES.text("preferences.appearance.chatTheme.preset.highContrast");
    };
  }

  private static final class ChatThemePresetRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      ChatThemeSettings.Preset presetValue =
          (value instanceof ChatThemeSettings.Preset typed)
              ? typed
              : ChatThemeSettings.Preset.DEFAULT;
      label.setText(labelFor(presetValue));
      return label;
    }
  }
}
