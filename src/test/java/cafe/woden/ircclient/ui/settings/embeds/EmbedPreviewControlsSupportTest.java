package cafe.woden.ircclient.ui.settings.embeds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.api.EmbedPreviewRuntimeConfigPort;
import cafe.woden.ircclient.config.api.EmbedPreviewRuntimeConfigPort.EmbedPreviewSnapshot;
import cafe.woden.ircclient.ui.settings.EmbedCardStyle;
import cafe.woden.ircclient.ui.settings.EmbedCardStyleBus;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class EmbedPreviewControlsSupportTest {

  @Test
  void readEmbedPreviewSettingsNormalizesImageAndCardValues() {
    ImageEmbedControls imageEmbeds = imageEmbedControls(true, true, -10, 480, true);
    LinkPreviewControls linkPreviews = linkPreviewControls(true, false, EmbedCardStyle.GLASSY);

    EmbedPreviewControlsSupport.EmbedPreviewSettings settings =
        EmbedPreviewControlsSupport.readEmbedPreviewSettings(imageEmbeds, linkPreviews);

    assertTrue(settings.imageEmbedsEnabled());
    assertTrue(settings.imageEmbedsCollapsedByDefault());
    assertEquals(0, settings.imageEmbedsMaxWidthPx());
    assertEquals(480, settings.imageEmbedsMaxHeightPx());
    assertTrue(settings.imageEmbedsAnimateGifs());
    assertTrue(settings.linkPreviewsEnabled());
    assertEquals(EmbedCardStyle.GLASSY, settings.embedCardStyle());
    assertTrue(settings.embedCardStyleChanged(EmbedCardStyle.DEFAULT));
  }

  @Test
  void rememberEmbedPreviewSettingsPersistsDisplayValuesAndUpdatesStyleBus() {
    EmbedPreviewRuntimeConfigPort runtimeConfig = mock(EmbedPreviewRuntimeConfigPort.class);
    EmbedCardStyleBus embedCardStyleBus = mock(EmbedCardStyleBus.class);
    EmbedPreviewControlsSupport.EmbedPreviewSettings settings =
        new EmbedPreviewControlsSupport.EmbedPreviewSettings(
            true, false, 640, 480, true, true, false, EmbedCardStyle.DENSER);

    EmbedPreviewControlsSupport.rememberEmbedPreviewSettings(
        runtimeConfig, embedCardStyleBus, settings);

    verify(runtimeConfig)
        .rememberEmbedPreviewSettings(
            new EmbedPreviewSnapshot(
                true, false, 640, 480, true, true, false, EmbedCardStyle.DENSER.token()));
    verify(embedCardStyleBus).set(EmbedCardStyle.DENSER);
  }

  private static ImageEmbedControls imageEmbedControls(
      boolean enabled, boolean collapsed, int maxWidth, int maxHeight, boolean animateGifs) {
    JCheckBox enabledBox = new JCheckBox();
    enabledBox.setSelected(enabled);
    JCheckBox collapsedBox = new JCheckBox();
    collapsedBox.setSelected(collapsed);
    JCheckBox animateBox = new JCheckBox();
    animateBox.setSelected(animateGifs);
    return new ImageEmbedControls(
        enabledBox, collapsedBox, spinner(maxWidth), spinner(maxHeight), animateBox, new JPanel());
  }

  private static LinkPreviewControls linkPreviewControls(
      boolean enabled, boolean collapsed, EmbedCardStyle cardStyle) {
    JCheckBox enabledBox = new JCheckBox();
    enabledBox.setSelected(enabled);
    JCheckBox collapsedBox = new JCheckBox();
    collapsedBox.setSelected(collapsed);
    JComboBox<EmbedCardStyle> styleCombo = new JComboBox<>(EmbedCardStyle.values());
    styleCombo.setSelectedItem(cardStyle);
    return new LinkPreviewControls(enabledBox, collapsedBox, styleCombo, new JPanel());
  }

  private static JSpinner spinner(int value) {
    return new JSpinner(new SpinnerNumberModel(value, -100_000, 100_000, 1));
  }
}
