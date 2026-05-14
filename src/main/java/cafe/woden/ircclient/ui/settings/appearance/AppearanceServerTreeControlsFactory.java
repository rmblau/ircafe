package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import javax.swing.JCheckBox;

final class AppearanceServerTreeControlsFactory {
  private AppearanceServerTreeControlsFactory() {}

  static AppearanceServerTreeControls build(UiSettings current) {
    ColorField unreadChannelColor =
        AppearanceColorFieldFactory.build(
            current != null ? current.serverTreeUnreadChannelColor() : null,
            "Pick a channel color for unread messages");
    ColorField highlightChannelColor =
        AppearanceColorFieldFactory.build(
            current != null ? current.serverTreeHighlightChannelColor() : null,
            "Pick a channel color for unread highlights/mentions");
    JCheckBox preserveDockLayoutBetweenSessions =
        new JCheckBox("Preserve dock layout between restarts");
    preserveDockLayoutBetweenSessions.setToolTipText(
        "When enabled, dock positions/splits are restored on next app launch.");
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
              controls.unreadChannelColor.hex.getText(), "Unread channel color");
      String highlightChannelColor =
          SettingsColorSupport.normalizeOptionalHexForApply(
              controls.highlightChannelColor.hex.getText(), "Highlight channel color");
      return new AppearanceControlsSupport.ServerTreeAppearanceSettings(
          unreadChannelColor,
          highlightChannelColor,
          controls.preserveDockLayoutBetweenSessions.isSelected());
    } catch (IllegalArgumentException ex) {
      throw new AppearanceControlsSupport.AppearanceSettingsException(
          "Invalid server tree color", ex.getMessage());
    }
  }

  static void remember(
      RuntimeConfigStore runtimeConfig,
      AppearanceControlsSupport.ServerTreeAppearanceSettings settings) {
    runtimeConfig.rememberServerTreeUnreadChannelColor(settings.unreadChannelColor());
    runtimeConfig.rememberServerTreeHighlightChannelColor(settings.highlightChannelColor());
    runtimeConfig.rememberPreserveDockLayout(settings.preserveDockLayoutBetweenSessions());
  }
}
