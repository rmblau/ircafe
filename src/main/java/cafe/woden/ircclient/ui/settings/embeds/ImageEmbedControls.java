package cafe.woden.ircclient.ui.settings.embeds;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;

public final class ImageEmbedControls {
  final JCheckBox enabled;
  final JCheckBox collapsed;
  final JSpinner maxWidth;
  final JSpinner maxHeight;
  final JCheckBox animateGifs;
  final JPanel panel;

  ImageEmbedControls(
      JCheckBox enabled,
      JCheckBox collapsed,
      JSpinner maxWidth,
      JSpinner maxHeight,
      JCheckBox animateGifs,
      JPanel panel) {
    this.enabled = enabled;
    this.collapsed = collapsed;
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
    this.animateGifs = animateGifs;
    this.panel = panel;
  }
}
