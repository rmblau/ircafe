package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class ChatDisplayControlsSupportTest {

  @Test
  void readTimestampSettingsDefaultsBlankFormatAndUpdatesField() {
    TimestampControls controls = timestampControls(true, "   ", true, false);

    ChatDisplayControlsSupport.TimestampSettings settings =
        ChatDisplayControlsSupport.readTimestampSettings(controls);

    assertTrue(settings.enabled());
    assertEquals("HH:mm:ss", settings.format());
    assertEquals("HH:mm:ss", controls.format.getText());
    assertTrue(settings.includeChatMessages());
  }

  @Test
  void readTimestampSettingsRejectsInvalidDateTimePattern() {
    TimestampControls controls = timestampControls(true, "HH:mm:ss 'unterminated", true, true);

    ChatDisplayControlsSupport.TimestampSettingsException ex =
        assertThrows(
            ChatDisplayControlsSupport.TimestampSettingsException.class,
            () -> ChatDisplayControlsSupport.readTimestampSettings(controls));

    assertEquals("Invalid timestamp format", ex.title());
    assertTrue(ex.getMessage().contains("Invalid timestamp format: HH:mm:ss 'unterminated"));
  }

  @Test
  void rememberTimestampSettingsPersistsTimestampValues() {
    RuntimeConfigStore runtimeConfig = mock(RuntimeConfigStore.class);
    ChatDisplayControlsSupport.TimestampSettings settings =
        new ChatDisplayControlsSupport.TimestampSettings(true, "HH:mm", false, true);

    ChatDisplayControlsSupport.rememberTimestampSettings(runtimeConfig, settings);

    verify(runtimeConfig).rememberTimestampsEnabled(true);
    verify(runtimeConfig).rememberTimestampFormat("HH:mm");
    verify(runtimeConfig).rememberTimestampsIncludeChatMessages(false);
    verify(runtimeConfig).rememberTimestampsIncludePresenceMessages(true);
  }

  @Test
  void readEmbedPreviewSettingsNormalizesImageAndCardValues() {
    ImageEmbedControls imageEmbeds = imageEmbedControls(true, true, -10, 480, true);
    LinkPreviewControls linkPreviews = linkPreviewControls(true, false, EmbedCardStyle.GLASSY);

    ChatDisplayControlsSupport.EmbedPreviewSettings settings =
        ChatDisplayControlsSupport.readEmbedPreviewSettings(imageEmbeds, linkPreviews);

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
    RuntimeConfigStore runtimeConfig = mock(RuntimeConfigStore.class);
    EmbedCardStyleBus embedCardStyleBus = mock(EmbedCardStyleBus.class);
    ChatDisplayControlsSupport.EmbedPreviewSettings settings =
        new ChatDisplayControlsSupport.EmbedPreviewSettings(
            true, false, 640, 480, true, true, false, EmbedCardStyle.DENSER);

    ChatDisplayControlsSupport.rememberEmbedPreviewSettings(
        runtimeConfig, embedCardStyleBus, settings);

    verify(runtimeConfig).rememberImageEmbedsEnabled(true);
    verify(runtimeConfig).rememberImageEmbedsCollapsedByDefault(false);
    verify(runtimeConfig).rememberImageEmbedsMaxWidthPx(640);
    verify(runtimeConfig).rememberImageEmbedsMaxHeightPx(480);
    verify(runtimeConfig).rememberImageEmbedsAnimateGifs(true);
    verify(runtimeConfig).rememberEmbedCardStyle(EmbedCardStyle.DENSER.token());
    verify(embedCardStyleBus).set(EmbedCardStyle.DENSER);
    verify(runtimeConfig).rememberLinkPreviewsEnabled(true);
    verify(runtimeConfig).rememberLinkPreviewsCollapsedByDefault(false);
  }

  private static TimestampControls timestampControls(
      boolean enabled,
      String format,
      boolean includeChatMessages,
      boolean includePresenceMessages) {
    JCheckBox enabledBox = new JCheckBox();
    enabledBox.setSelected(enabled);
    JCheckBox includeChat = new JCheckBox();
    includeChat.setSelected(includeChatMessages);
    JCheckBox includePresence = new JCheckBox();
    includePresence.setSelected(includePresenceMessages);
    return new TimestampControls(
        enabledBox, new JTextField(format), includeChat, includePresence, new JPanel());
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
