package cafe.woden.ircclient.ui.settings.embeds;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.EmbedCardStyle;
import cafe.woden.ircclient.ui.settings.EmbedCardStyleBus;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

public final class EmbedPreviewControlsSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private EmbedPreviewControlsSupport() {}

  public static ImageEmbedControls buildImageEmbedControls(
      UiSettings current, List<AutoCloseable> closeables) {
    JCheckBox imageEmbeds = new JCheckBox(MESSAGES.text("preferences.embeds.image.enabled"));
    imageEmbeds.setSelected(current.imageEmbedsEnabled());
    imageEmbeds.setToolTipText(
        MESSAGES.text("preferences.embeds.image.enabled.tooltip"));

    JCheckBox imageEmbedsCollapsed =
        new JCheckBox(MESSAGES.text("preferences.embeds.image.collapsed"));
    imageEmbedsCollapsed.setSelected(current.imageEmbedsCollapsedByDefault());
    imageEmbedsCollapsed.setToolTipText(
        MESSAGES.text("preferences.embeds.image.collapsed.tooltip"));
    imageEmbedsCollapsed.setEnabled(imageEmbeds.isSelected());
    JSpinner imageMaxWidth =
        PreferencesUiSupport.numberSpinner(
            current.imageEmbedsMaxWidthPx(), 0, 4096, 10, closeables);
    imageMaxWidth.setToolTipText(
        MESSAGES.text("preferences.embeds.image.maxWidth.tooltip"));
    imageMaxWidth.setEnabled(imageEmbeds.isSelected());
    JSpinner imageMaxHeight =
        PreferencesUiSupport.numberSpinner(
            current.imageEmbedsMaxHeightPx(), 0, 4096, 10, closeables);
    imageMaxHeight.setToolTipText(
        MESSAGES.text("preferences.embeds.image.maxHeight.tooltip"));
    imageMaxHeight.setEnabled(imageEmbeds.isSelected());

    JCheckBox animateGifs = new JCheckBox(MESSAGES.text("preferences.embeds.image.animateGifs"));
    animateGifs.setSelected(current.imageEmbedsAnimateGifs());
    animateGifs.setToolTipText(MESSAGES.text("preferences.embeds.image.animateGifs.tooltip"));
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
        new JPanel(MigLayouts.fillXWrap(0, 2, "[grow,fill]8[nogrid]", MigLayouts.rows(5, 4)));
    imagePanel.setOpaque(false);
    imagePanel.add(imageEmbeds, MigConstraints.spanXWrap(2));
    imagePanel.add(imageEmbedsCollapsed, MigConstraints.spanXWrap(2));
    imagePanel.add(new JLabel(MESSAGES.text("preferences.embeds.image.maxWidth")));
    imagePanel.add(imageMaxWidth, MigConstraints.width(110));
    imagePanel.add(new JLabel(MESSAGES.text("preferences.embeds.image.maxHeight")));
    imagePanel.add(imageMaxHeight, MigConstraints.width(110));
    imagePanel.add(animateGifs, MigConstraints.spanXWrap(2));

    return new ImageEmbedControls(
        imageEmbeds, imageEmbedsCollapsed, imageMaxWidth, imageMaxHeight, animateGifs, imagePanel);
  }

  public static LinkPreviewControls buildLinkPreviewControls(
      UiSettings current, EmbedCardStyle currentEmbedCardStyle) {
    JCheckBox linkPreviews = new JCheckBox(MESSAGES.text("preferences.embeds.link.enabled"));
    linkPreviews.setSelected(current.linkPreviewsEnabled());
    linkPreviews.setToolTipText(
        MESSAGES.text("preferences.embeds.link.enabled.tooltip"));

    JCheckBox linkPreviewsCollapsed =
        new JCheckBox(MESSAGES.text("preferences.embeds.link.collapsed"));
    linkPreviewsCollapsed.setSelected(current.linkPreviewsCollapsedByDefault());
    linkPreviewsCollapsed.setToolTipText(
        MESSAGES.text("preferences.embeds.link.collapsed.tooltip"));
    linkPreviewsCollapsed.setEnabled(linkPreviews.isSelected());
    linkPreviews.addActionListener(
        e -> linkPreviewsCollapsed.setEnabled(linkPreviews.isSelected()));

    JComboBox<EmbedCardStyle> cardStyle = new JComboBox<>(EmbedCardStyle.values());
    cardStyle.setSelectedItem(
        currentEmbedCardStyle != null ? currentEmbedCardStyle : EmbedCardStyle.DEFAULT);
    cardStyle.setToolTipText(
        MESSAGES.text("preferences.embeds.link.cardStyle.tooltip"));

    JPanel linkPanel = new JPanel(MigLayouts.singleColumn(MigLayouts.rowGaps(4, 8)));
    linkPanel.setOpaque(false);
    linkPanel.add(linkPreviews);
    linkPanel.add(linkPreviewsCollapsed);
    JPanel styleRow = new JPanel(MigLayouts.insets0(MigLayoutConstraints.LEADING_GROW_FILL, "[]"));
    styleRow.setOpaque(false);
    styleRow.add(new JLabel(MESSAGES.text("preferences.embeds.link.cardStyle")));
    styleRow.add(cardStyle, MigConstraints.width(180));
    linkPanel.add(styleRow, MigConstraints.growX());

    return new LinkPreviewControls(linkPreviews, linkPreviewsCollapsed, cardStyle, linkPanel);
  }

  public static EmbedPreviewSettings readEmbedPreviewSettings(
      ImageEmbedControls imageEmbeds, LinkPreviewControls linkPreviews) {
    EmbedCardStyle cardStyle =
        PreferencesUiSupport.selectedComboItem(
            linkPreviews.cardStyle, EmbedCardStyle.class, EmbedCardStyle.DEFAULT);

    return new EmbedPreviewSettings(
        imageEmbeds.enabled.isSelected(),
        imageEmbeds.collapsed.isSelected(),
        PreferencesUiSupport.spinnerInt(imageEmbeds.maxWidth),
        PreferencesUiSupport.spinnerInt(imageEmbeds.maxHeight),
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
      imageEmbedsMaxWidthPx =
          SettingsRangeSupport.normalizeImageEmbedDimensionPx(imageEmbedsMaxWidthPx);
      imageEmbedsMaxHeightPx =
          SettingsRangeSupport.normalizeImageEmbedDimensionPx(imageEmbedsMaxHeightPx);
      if (embedCardStyle == null) embedCardStyle = EmbedCardStyle.DEFAULT;
    }

    public boolean embedCardStyleChanged(EmbedCardStyle previous) {
      EmbedCardStyle normalizedPrevious = previous != null ? previous : EmbedCardStyle.DEFAULT;
      return normalizedPrevious != embedCardStyle;
    }
  }
}
