package cafe.woden.ircclient.ui.settings;

import javax.swing.JButton;

final class PreferencesDialogActionButtonsSupport {
  private PreferencesDialogActionButtonsSupport() {}

  static Buttons build() {
    return new Buttons(
        PreferencesUiSupport.buttonWithIcon("Apply", "check"),
        PreferencesUiSupport.buttonWithIcon("OK", "check"),
        PreferencesUiSupport.buttonWithIcon("Cancel", "close"));
  }

  record Buttons(JButton apply, JButton ok, JButton cancel) {}
}
