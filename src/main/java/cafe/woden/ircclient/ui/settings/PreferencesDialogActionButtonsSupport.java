package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.ui.localization.UiMessages;
import java.util.Objects;
import javax.swing.JButton;

final class PreferencesDialogActionButtonsSupport {
  private PreferencesDialogActionButtonsSupport() {}

  static Buttons build(UiMessages messages) {
    UiMessages bundle = Objects.requireNonNull(messages, "messages");
    return new Buttons(
        PreferencesUiSupport.buttonWithIcon(bundle.text("preferences.button.apply"), "check"),
        PreferencesUiSupport.buttonWithIcon(bundle.text("preferences.button.ok"), "check"),
        PreferencesUiSupport.buttonWithIcon(bundle.text("preferences.button.cancel"), "close"));
  }

  record Buttons(JButton apply, JButton ok, JButton cancel) {}
}
