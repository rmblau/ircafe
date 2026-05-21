package cafe.woden.ircclient.ui.settings.timestamp;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class TimestampControls {
  final JCheckBox enabled;
  final JTextField format;
  final JCheckBox includeChatMessages;
  final JCheckBox includePresenceMessages;
  private final JPanel panel;

  TimestampControls(
      JCheckBox enabled,
      JTextField format,
      JCheckBox includeChatMessages,
      JCheckBox includePresenceMessages,
      JPanel panel) {
    this.enabled = enabled;
    this.format = format;
    this.includeChatMessages = includeChatMessages;
    this.includePresenceMessages = includePresenceMessages;
    this.panel = panel;
  }

  public JPanel panel() {
    return panel;
  }
}
