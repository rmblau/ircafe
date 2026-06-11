package cafe.woden.ircclient.ui.chat.fold;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import cafe.woden.ircclient.ui.util.UiFontKeys;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Summary row used when history/backfill filtering would otherwise generate too many
 * placeholder/hint rows.
 *
 * <p>It aggregates the remainder of hidden lines for the current load into a single counter.
 */
public class FilteredOverflowComponent extends JPanel implements FilteredLineComponent {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private int count = 0;

  // Optional base font from the transcript; if set, we derive italic variants from it.
  private Font transcriptBaseFont;

  // Filter context for tooltip polish.
  private String filterRuleLabel;
  private boolean multipleRules;
  private final List<String> unionTags = new ArrayList<>();

  // UI tuning
  private int maxTagsInTooltip = 12;

  private final JLabel label = new JLabel();

  public FilteredOverflowComponent() {
    setOpaque(false);
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(2, 0, 2, 0));

    label.setOpaque(false);
    label.setBorder(new EmptyBorder(0, 0, 0, 0));

    updateText();
    refreshTooltip();
    add(label, BorderLayout.CENTER);
  }

  public int count() {
    return count;
  }

  /** Allows the transcript owner to provide a consistent base font for embedded components. */
  public void setTranscriptFont(Font base) {
    this.transcriptBaseFont = base;
    applyDimItalic(label);
    revalidate();
    repaint();
  }

  /**
   * Limits how many tags are listed in the tooltip tag summary.
   *
   * <p>0 disables tag listing entirely.
   */
  public void setMaxTagsInTooltip(int max) {
    if (max < 0) max = 0;
    if (max > 500) max = 500;
    this.maxTagsInTooltip = max;
    refreshTooltip();
  }

  /**
   * Provides extra tooltip context: which rule(s) filtered this batch and which tags were present.
   */
  public void setFilterDetails(String ruleLabel, boolean multiple, Collection<String> tags) {
    this.filterRuleLabel = (ruleLabel == null || ruleLabel.isBlank()) ? null : ruleLabel;
    this.multipleRules = multiple;

    this.unionTags.clear();
    if (tags != null) {
      for (String t : tags) {
        if (t == null) continue;
        String s = t.trim();
        if (!s.isEmpty()) this.unionTags.add(s);
      }
    }

    refreshTooltip();
  }

  /** Adds one more filtered line to the overflow counter. */
  public void addFilteredLine() {
    count++;
    updateText();
    refreshTooltip();
    revalidate();
    repaint();
  }

  private void updateText() {
    // Keep it short and clear: this row exists because we hit the per-batch placeholder cap.
    label.setText(
        MESSAGES.text(
            count == 1
                ? "chat.fold.filtered.overflow.moreLine"
                : "chat.fold.filtered.overflow.moreLines",
            count));
    applyDimItalic(label);
  }

  private void refreshTooltip() {
    setToolTipText(
        buildTooltipHtml(count, filterRuleLabel, multipleRules, unionTags, maxTagsInTooltip));
  }

  private static String buildTooltipHtml(
      int count, String ruleLabel, boolean multiple, List<String> tags, int maxTags) {
    if (count <= 0) return null;

    StringBuilder sb = new StringBuilder();
    sb.append("<html>");

    if (ruleLabel != null && !ruleLabel.isBlank()) {
      sb.append(MESSAGES.text("chat.fold.filtered.tooltip.filteredBy", escapeHtml(ruleLabel)));
      if (multiple) sb.append(MESSAGES.text("chat.fold.filtered.tooltip.others"));
    } else {
      sb.append(MESSAGES.text("chat.fold.filtered.tooltip.filtered"));
      if (multiple) sb.append(MESSAGES.text("chat.fold.filtered.tooltip.multipleRules"));
    }

    if (tags != null && !tags.isEmpty() && maxTags > 0) {
      sb.append("<br/>");
      sb.append(MESSAGES.text("chat.fold.filtered.tooltip.tags", tagsSummaryHtml(tags, maxTags)));
    }

    sb.append("<br/><br/>");
    sb.append(MESSAGES.text("chat.fold.filtered.tooltip.hiddenLinesOverflow", count));
    sb.append("<br/>");
    sb.append(MESSAGES.text("chat.fold.filtered.tooltip.placeholderLimit"));

    sb.append("</html>");
    return sb.toString();
  }

  private static String tagsSummaryHtml(List<String> tags, int maxTags) {
    if (tags == null || tags.isEmpty()) return "";
    int limit = Math.max(0, maxTags);
    if (limit <= 0) return "";
    StringBuilder sb = new StringBuilder();
    int shown = 0;
    for (String t : tags) {
      if (t == null) continue;
      if (shown > 0) sb.append(", ");
      if (shown >= limit) {
        sb.append(MESSAGES.text("chat.fold.filtered.tags.more", tags.size() - shown));
        break;
      }
      sb.append(escapeHtml(t));
      shown++;
    }
    return sb.toString();
  }

  private void applyDimItalic(JLabel l) {
    if (l == null) return;
    Color dim = UIManager.getColor(UiColorKeys.LABEL_DISABLED_FOREGROUND);
    if (dim != null) l.setForeground(dim);

    Font base =
        (transcriptBaseFont != null)
            ? transcriptBaseFont
            : UIManager.getFont(UiFontKeys.LABEL_FONT);
    if (base != null) {
      l.setFont(base.deriveFont(Font.ITALIC));
    }
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    int w = availableWidth();
    if (w > 0) {
      return new Dimension(w, d.height);
    }
    return d;
  }

  @Override
  public Dimension getMaximumSize() {
    Dimension d = getPreferredSize();
    return new Dimension(Integer.MAX_VALUE, d.height);
  }

  private int availableWidth() {
    Container p = getParent();
    if (p == null) return -1;
    int w = p.getWidth();
    if (w <= 0) return -1;

    Insets insets =
        (p instanceof JComponent) ? ((JComponent) p).getInsets() : new Insets(0, 0, 0, 0);

    w = w - insets.left - insets.right;
    return Math.max(0, w);
  }

  private static String escapeHtml(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
