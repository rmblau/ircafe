package cafe.woden.ircclient.ui.chat.transcript.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptColorSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.awt.Color;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock(value = "UIManager", mode = ResourceAccessMode.READ_WRITE)
class ChatTranscriptRuntimeSettingsSupportTest {

  private Object backgroundSnapshot;
  private Object foregroundSnapshot;

  @BeforeEach
  void snapshotUiManager() {
    backgroundSnapshot = UIManager.get("TextPane.background");
    foregroundSnapshot = UIManager.get("TextPane.foreground");
  }

  @AfterEach
  void restoreUiManager() {
    UIManager.put("TextPane.background", backgroundSnapshot);
    UIManager.put("TextPane.foreground", foregroundSnapshot);
  }

  @Test
  void defaultsStaySafeWhenSettingsAreUnavailable() {
    UiSettingsBus bus = mock(UiSettingsBus.class);
    when(bus.get()).thenThrow(new IllegalStateException("boom"));
    ChatTranscriptRuntimeSettingsSupport support =
        new ChatTranscriptRuntimeSettingsSupport(bus, new ChatStyles(null));

    assertEquals(
        ChatTranscriptRuntimeSettingsSupport.DEFAULT_TRANSCRIPT_MAX_LINES_PER_TARGET,
        support.transcriptMaxLinesPerTarget());
    assertTrue(support.outgoingDeliveryIndicatorsEnabled());
    assertTrue(support.presenceFoldsEnabled());
    assertFalse(support.timestampsIncludeChatMessages());
    assertFalse(support.timestampsIncludePresenceMessages());
    assertFalse(support.chatHistoryDeferRichTextDuringBatch());
    assertFalse(support.imageEmbedsEnabled());
    assertFalse(support.linkPreviewsEnabled());
  }

  @Test
  void transcriptMaxLinesIsClampedToSupportedBounds() {
    UiSettingsBus bus = mock(UiSettingsBus.class);
    UiSettings settings = mock(UiSettings.class);
    when(bus.get()).thenReturn(settings);
    when(settings.chatTranscriptMaxLinesPerTarget()).thenReturn(500_000);
    ChatTranscriptRuntimeSettingsSupport support =
        new ChatTranscriptRuntimeSettingsSupport(bus, new ChatStyles(null));

    assertEquals(
        ChatTranscriptRuntimeSettingsSupport.MAX_TRANSCRIPT_LINES_PER_TARGET,
        support.transcriptMaxLinesPerTarget());

    when(settings.chatTranscriptMaxLinesPerTarget()).thenReturn(-1);
    assertEquals(0, support.transcriptMaxLinesPerTarget());
  }

  @Test
  void outgoingLineColorIsAdjustedToRemainReadable() {
    UIManager.put("TextPane.background", Color.WHITE);
    UIManager.put("TextPane.foreground", Color.BLACK);
    ChatTranscriptRuntimeSettingsSupport support =
        new ChatTranscriptRuntimeSettingsSupport(null, new ChatStyles(null));
    UiSettings settings = mock(UiSettings.class);
    when(settings.clientLineColorEnabled()).thenReturn(true);
    when(settings.clientLineColor()).thenReturn("#EAF2FF");

    Color adjusted = support.configuredOutgoingLineColor(settings);

    assertNotNull(adjusted);
    assertTrue(ChatTranscriptColorSupport.contrastRatio(adjusted, Color.WHITE) >= 4.5);
  }

  @Test
  void booleanFlagsReflectSettingsWhenPresent() {
    UiSettingsBus bus = mock(UiSettingsBus.class);
    UiSettings settings = mock(UiSettings.class);
    when(bus.get()).thenReturn(settings);
    when(settings.outgoingDeliveryIndicatorsEnabled()).thenReturn(false);
    when(settings.presenceFoldsEnabled()).thenReturn(false);
    when(settings.timestampsIncludeChatMessages()).thenReturn(true);
    when(settings.timestampsIncludePresenceMessages()).thenReturn(true);
    when(settings.chatHistoryDeferRichTextDuringBatch()).thenReturn(true);
    when(settings.imageEmbedsEnabled()).thenReturn(true);
    when(settings.linkPreviewsEnabled()).thenReturn(true);
    ChatTranscriptRuntimeSettingsSupport support =
        new ChatTranscriptRuntimeSettingsSupport(bus, new ChatStyles(null));

    assertFalse(support.outgoingDeliveryIndicatorsEnabled());
    assertFalse(support.presenceFoldsEnabled());
    assertTrue(support.timestampsIncludeChatMessages());
    assertTrue(support.timestampsIncludePresenceMessages());
    assertTrue(support.chatHistoryDeferRichTextDuringBatch());
    assertTrue(support.imageEmbedsEnabled());
    assertTrue(support.linkPreviewsEnabled());
  }
}
