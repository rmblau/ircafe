package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.config.api.AppearanceRuntimeConfigPort;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import javax.swing.JCheckBox;

final class AppearanceServerTreeControlsFactory {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private AppearanceServerTreeControlsFactory() {}

  static AppearanceServerTreeControls build(UiSettings current) {
    ColorField unreadChannelColor =
        AppearanceColorFieldFactory.build(
            current != null ? current.serverTreeUnreadChannelColor() : null,
            MESSAGES.text("preferences.appearance.serverTree.picker.unread"));
    ColorField highlightChannelColor =
        AppearanceColorFieldFactory.build(
            current != null ? current.serverTreeHighlightChannelColor() : null,
            MESSAGES.text("preferences.appearance.serverTree.picker.highlight"));
    JCheckBox preserveDockLayoutBetweenSessions =
        new JCheckBox(MESSAGES.text("preferences.appearance.serverTree.preserveDockLayout"));
    preserveDockLayoutBetweenSessions.setToolTipText(
        MESSAGES.text("preferences.appearance.serverTree.preserveDockLayout.tooltip"));
    preserveDockLayoutBetweenSessions.setSelected(
        current != null && current.preserveDockLayoutBetweenSessions());
    return new AppearanceServerTreeControls(
        unreadChannelColor, highlightChannelColor, preserveDockLayoutBetweenSessions);
  }

  static AppearanceControlsSupport.ServerTreeAppearanceSettings read(
      AppearanceServerTreeControls controls)
      throws AppearanceControlsSupport.AppearanceSettingsException {
    try {
      String unreadChannelColor =
          SettingsColorSupport.normalizeOptionalHexForApply(
              controls.unreadChannelColor.hex.getText(),
              MESSAGES.text("preferences.appearance.field.unreadChannelColor"));
      String highlightChannelColor =
          SettingsColorSupport.normalizeOptionalHexForApply(
              controls.highlightChannelColor.hex.getText(),
              MESSAGES.text("preferences.appearance.field.highlightChannelColor"));
      return new AppearanceControlsSupport.ServerTreeAppearanceSettings(
          unreadChannelColor,
          highlightChannelColor,
          controls.preserveDockLayoutBetweenSessions.isSelected());
    } catch (IllegalArgumentException ex) {
      throw new AppearanceControlsSupport.AppearanceSettingsException(
          MESSAGES.text("preferences.appearance.validation.serverTreeColor.title"),
          ex.getMessage());
    }
  }

  static void remember(
      AppearanceRuntimeConfigPort runtimeConfig,
      AppearanceControlsSupport.ServerTreeAppearanceSettings settings) {
    runtimeConfig.rememberServerTreeUnreadChannelColor(settings.unreadChannelColor());
    runtimeConfig.rememberServerTreeHighlightChannelColor(settings.highlightChannelColor());
    runtimeConfig.rememberPreserveDockLayout(settings.preserveDockLayoutBetweenSessions());
  }
}
