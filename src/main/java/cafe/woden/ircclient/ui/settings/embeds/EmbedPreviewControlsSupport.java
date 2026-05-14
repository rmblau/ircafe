package cafe.woden.ircclient.ui.settings.embeds;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.settings.EmbedCardStyle;
import cafe.woden.ircclient.ui.settings.EmbedCardStyleBus;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import net.miginfocom.swing.MigLayout;

public final class EmbedPreviewControlsSupport {
  private EmbedPreviewControlsSupport() {}

  public static ImageEmbedControls buildImageEmbedControls(
      UiSettings current, List<AutoCloseable> closeables) {
    JCheckBox imageEmbeds = new JCheckBox("Enable inline image embeds (direct links)");
    imageEmbeds.setSelected(current.imageEmbedsEnabled());
    imageEmbeds.setToolTipText(
        "If enabled, IRCafe will download and render images from direct image URLs in chat.");

    JCheckBox imageEmbedsCollapsed = new JCheckBox("Collapse inline images by default");
    imageEmbedsCollapsed.setSelected(current.imageEmbedsCollapsedByDefault());
    imageEmbedsCollapsed.setToolTipText(
        "If enabled, newly inserted inline images start collapsed (header shown; click to expand).");
    imageEmbedsCollapsed.setEnabled(imageEmbeds.isSelected());
    JSpinner imageMaxWidth =
        PreferencesUiSupport.numberSpinner(
            current.imageEmbedsMaxWidthPx(), 0, 4096, 10, closeables);
    imageMaxWidth.setToolTipText(
        "Maximum width for inline images (pixels).\n"
            + "If 0, IRCafe will only scale images down to fit the chat viewport.");
    imageMaxWidth.setEnabled(imageEmbeds.isSelected());
    JSpinner imageMaxHeight =
        PreferencesUiSupport.numberSpinner(
            current.imageEmbedsMaxHeightPx(), 0, 4096, 10, closeables);
    imageMaxHeight.setToolTipText(
        "Maximum height for inline images (pixels).\n"
            + "If 0, IRCafe will only scale images down based on viewport width (and max width cap, if set).");
    imageMaxHeight.setEnabled(imageEmbeds.isSelected());

    JCheckBox animateGifs = new JCheckBox("Animate GIFs");
    animateGifs.setSelected(current.imageEmbedsAnimateGifs());
    animateGifs.setToolTipText("If disabled, animated GIFs render as a still image (first frame).");
    animateGifs.setEnabled(imageEmbeds.isSelected());

    imageEmbeds.addActionListener(
        e -> {
          boolean enabled = imageEmbeds.isSelected();
          imageEmbedsCollapsed.setEnabled(enabled);
          imageMaxWidth.setEnabled(enabled);
          imageMaxHeight.setEnabled(enabled);
          animateGifs.setEnabled(enabled);
        });

    JPanel imagePanel =
        new JPanel(
            new MigLayout("insets 0, fillx, wrap 2", "[grow,fill]8[nogrid]", "[]4[]4[]4[]4[]"));
    imagePanel.setOpaque(false);
    imagePanel.add(imageEmbeds, "span 2, wrap");
    imagePanel.add(imageEmbedsCollapsed, "span 2, wrap");
    imagePanel.add(new JLabel("Max image width (px, 0 = no limit):"));
    imagePanel.add(imageMaxWidth, "w 110!");
    imagePanel.add(new JLabel("Max image height (px, 0 = no limit):"));
    imagePanel.add(imageMaxHeight, "w 110!");
    imagePanel.add(animateGifs, "span 2, wrap");

    return new ImageEmbedControls(
        imageEmbeds, imageEmbedsCollapsed, imageMaxWidth, imageMaxHeight, animateGifs, imagePanel);
  }

  public static LinkPreviewControls buildLinkPreviewControls(
      UiSettings current, EmbedCardStyle currentEmbedCardStyle) {
    JCheckBox linkPreviews = new JCheckBox("Enable link previews (OpenGraph cards)");
    linkPreviews.setSelected(current.linkPreviewsEnabled());
    linkPreviews.setToolTipText(
        "If enabled, IRCafe will fetch page metadata (title/description/image) and show a preview card under messages.\n"
            + "Note: this makes network requests to the linked sites.");

    JCheckBox linkPreviewsCollapsed = new JCheckBox("Collapse link previews by default");
    linkPreviewsCollapsed.setSelected(current.linkPreviewsCollapsedByDefault());
    linkPreviewsCollapsed.setToolTipText(
        "If enabled, newly inserted link previews start collapsed (header shown; click to expand).");
    linkPreviewsCollapsed.setEnabled(linkPreviews.isSelected());
    linkPreviews.addActionListener(
        e -> linkPreviewsCollapsed.setEnabled(linkPreviews.isSelected()));

    JComboBox<EmbedCardStyle> cardStyle = new JComboBox<>(EmbedCardStyle.values());
    cardStyle.setSelectedItem(
        currentEmbedCardStyle != null ? currentEmbedCardStyle : EmbedCardStyle.DEFAULT);
    cardStyle.setToolTipText(
        "Visual preset for inline cards used by link previews and image embeds.");

    JPanel linkPanel =
        new JPanel(new MigLayout("insets 0, fillx, wrap 1", "[grow,fill]", "[]4[]8[]"));
    linkPanel.setOpaque(false);
    linkPanel.add(linkPreviews);
    linkPanel.add(linkPreviewsCollapsed);
    JPanel styleRow = new JPanel(new MigLayout("insets 0", "[][grow,fill]", "[]"));
    styleRow.setOpaque(false);
    styleRow.add(new JLabel("Card style"));
    styleRow.add(cardStyle, "w 180!");
    linkPanel.add(styleRow, "growx");

    return new LinkPreviewControls(linkPreviews, linkPreviewsCollapsed, cardStyle, linkPanel);
  }

  public static EmbedPreviewSettings readEmbedPreviewSettings(
      ImageEmbedControls imageEmbeds, LinkPreviewControls linkPreviews) {
    EmbedCardStyle cardStyle =
        linkPreviews.cardStyle.getSelectedItem() instanceof EmbedCardStyle style
            ? style
            : EmbedCardStyle.DEFAULT;

    return new EmbedPreviewSettings(
        imageEmbeds.enabled.isSelected(),
        imageEmbeds.collapsed.isSelected(),
        ((Number) imageEmbeds.maxWidth.getValue()).intValue(),
        ((Number) imageEmbeds.maxHeight.getValue()).intValue(),
        imageEmbeds.animateGifs.isSelected(),
        linkPreviews.enabled.isSelected(),
        linkPreviews.collapsed.isSelected(),
        cardStyle);
  }

  public static void rememberEmbedPreviewSettings(
      RuntimeConfigStore runtimeConfig,
      EmbedCardStyleBus embedCardStyleBus,
      EmbedPreviewSettings settings) {
    runtimeConfig.rememberImageEmbedsEnabled(settings.imageEmbedsEnabled());
    runtimeConfig.rememberImageEmbedsCollapsedByDefault(settings.imageEmbedsCollapsedByDefault());
    runtimeConfig.rememberImageEmbedsMaxWidthPx(settings.imageEmbedsMaxWidthPx());
    runtimeConfig.rememberImageEmbedsMaxHeightPx(settings.imageEmbedsMaxHeightPx());
    runtimeConfig.rememberImageEmbedsAnimateGifs(settings.imageEmbedsAnimateGifs());
    runtimeConfig.rememberEmbedCardStyle(settings.embedCardStyle().token());
    if (embedCardStyleBus != null) {
      embedCardStyleBus.set(settings.embedCardStyle());
    }
    runtimeConfig.rememberLinkPreviewsEnabled(settings.linkPreviewsEnabled());
    runtimeConfig.rememberLinkPreviewsCollapsedByDefault(settings.linkPreviewsCollapsedByDefault());
  }

  public record EmbedPreviewSettings(
      boolean imageEmbedsEnabled,
      boolean imageEmbedsCollapsedByDefault,
      int imageEmbedsMaxWidthPx,
      int imageEmbedsMaxHeightPx,
      boolean imageEmbedsAnimateGifs,
      boolean linkPreviewsEnabled,
      boolean linkPreviewsCollapsedByDefault,
      EmbedCardStyle embedCardStyle) {
    public EmbedPreviewSettings {
      if (imageEmbedsMaxWidthPx < 0) imageEmbedsMaxWidthPx = 0;
      if (imageEmbedsMaxHeightPx < 0) imageEmbedsMaxHeightPx = 0;
      if (embedCardStyle == null) embedCardStyle = EmbedCardStyle.DEFAULT;
    }

    public boolean embedCardStyleChanged(EmbedCardStyle previous) {
      EmbedCardStyle normalizedPrevious = previous != null ? previous : EmbedCardStyle.DEFAULT;
      return normalizedPrevious != embedCardStyle;
    }
  }
}
