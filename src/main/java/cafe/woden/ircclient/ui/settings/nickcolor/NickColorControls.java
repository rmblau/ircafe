package cafe.woden.ircclient.ui.settings.nickcolor;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;

public final class NickColorControls {
  final JCheckBox enabled;
  final JSpinner minContrast;
  final JButton overrides;
  private final JPanel panel;

  NickColorControls(JCheckBox enabled, JSpinner minContrast, JButton overrides, JPanel panel) {
    this.enabled = enabled;
    this.minContrast = minContrast;
    this.overrides = overrides;
    this.panel = panel;
  }

  public JPanel panel() {
    return panel;
  }
}
