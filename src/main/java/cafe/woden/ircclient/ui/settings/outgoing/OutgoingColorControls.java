package cafe.woden.ircclient.ui.settings.outgoing;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class OutgoingColorControls {
  final JCheckBox enabled;
  final JTextField hex;
  final JLabel preview;
  private final JPanel panel;

  OutgoingColorControls(JCheckBox enabled, JTextField hex, JLabel preview, JPanel panel) {
    this.enabled = enabled;
    this.hex = hex;
    this.preview = preview;
    this.panel = panel;
  }

  public JPanel panel() {
    return panel;
  }
}
