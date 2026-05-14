package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettings;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSlider;

final class AppearanceChatThemeControlsFactory {
  private AppearanceChatThemeControlsFactory() {}

  static ChatThemeControls build(ChatThemeSettings current) {
    JComboBox<ChatThemeSettings.Preset> preset = createPresetCombo(current);
    ColorField timestamp =
        AppearanceColorFieldFactory.build(
            current != null ? current.timestampColor() : null, "Pick a timestamp color");
    ColorField system =
        AppearanceColorFieldFactory.build(
            current != null ? current.systemColor() : null, "Pick a system/status color");
    ColorField mention =
        AppearanceColorFieldFactory.build(
            current != null ? current.mentionBgColor() : null, "Pick a mention highlight color");
    ColorField message =
        AppearanceColorFieldFactory.build(
            current != null ? current.messageColor() : null, "Pick a user message color");
    ColorField notice =
        AppearanceColorFieldFactory.build(
            current != null ? current.noticeColor() : null, "Pick a notice message color");
    ColorField action =
        AppearanceColorFieldFactory.build(
            current != null ? current.actionColor() : null, "Pick an action message color");
    ColorField error =
        AppearanceColorFieldFactory.build(
            current != null ? current.errorColor() : null, "Pick an error message color");
    ColorField presence =
        AppearanceColorFieldFactory.build(
            current != null ? current.presenceColor() : null, "Pick a presence message color");

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
    JSlider mentionStrength = new JSlider(0, 100, Math.max(0, Math.min(100, strength)));
    mentionStrength.setMajorTickSpacing(25);
    mentionStrength.setMinorTickSpacing(5);
    mentionStrength.setPaintTicks(false);
    mentionStrength.setPaintLabels(false);
    mentionStrength.setToolTipText(
        "How strong the mention highlight is when using the preset highlight (0-100). Defaults to 35.");
    return mentionStrength;
  }

  private static String labelFor(ChatThemeSettings.Preset presetValue) {
    return switch (presetValue) {
      case DEFAULT -> "Default (follow theme)";
      case SOFT -> "Soft";
      case ACCENTED -> "Accented";
      case HIGH_CONTRAST -> "High contrast";
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
