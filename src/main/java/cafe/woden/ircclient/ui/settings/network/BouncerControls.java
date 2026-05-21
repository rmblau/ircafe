package cafe.woden.ircclient.ui.settings.network;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

public final class BouncerControls {
  final JCheckBox preferLoginHint;
  final JTextField loginTemplate;

  BouncerControls(JCheckBox preferLoginHint, JTextField loginTemplate) {
    this.preferLoginHint = preferLoginHint;
    this.loginTemplate = loginTemplate;
  }
}
