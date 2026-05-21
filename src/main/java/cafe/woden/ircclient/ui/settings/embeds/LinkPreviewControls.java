package cafe.woden.ircclient.ui.settings.embeds;

import cafe.woden.ircclient.ui.settings.EmbedCardStyle;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public final class LinkPreviewControls {
  final JCheckBox enabled;
  final JCheckBox collapsed;
  final JComboBox<EmbedCardStyle> cardStyle;
  final JPanel panel;

  LinkPreviewControls(
      JCheckBox enabled, JCheckBox collapsed, JComboBox<EmbedCardStyle> cardStyle, JPanel panel) {
    this.enabled = enabled;
    this.collapsed = collapsed;
    this.cardStyle = cardStyle;
    this.panel = panel;
  }
}
