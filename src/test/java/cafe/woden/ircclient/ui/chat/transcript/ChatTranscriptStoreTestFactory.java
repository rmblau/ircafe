package cafe.woden.ircclient.ui.chat.transcript;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.irc.roster.UserListStore;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.settings.MemoryUsageDisplayMode;
import cafe.woden.ircclient.ui.settings.NotificationBackendMode;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.util.List;

/** Test factory helpers for {@link ChatTranscriptStore}. */
final class ChatTranscriptStoreTestFactory {

  private ChatTranscriptStoreTestFactory() {}

  static ChatTranscriptStore newStore() {
    return newStore(new ChatStyles(null), null, null);
  }

  static ChatTranscriptStore newStoreWithTranscriptCap(int maxLines) {
    return newStoreWithSettings(settingsWithTranscriptCap(maxLines), null);
  }

  static ChatTranscriptStore newStoreWithTranscriptCapAndDeliveryIndicators(
      int maxLines, boolean enabled) {
    return newStoreWithSettings(settingsWithTranscriptCap(maxLines, enabled), null);
  }

  static ChatTranscriptStore newStoreWithTranscriptCapAndUserList(
      int maxLines, UserListStore userListStore) {
    return newStoreWithSettings(settingsWithTranscriptCap(maxLines), userListStore);
  }

  private static ChatTranscriptStore newStoreWithSettings(
      UiSettings settings, UserListStore userListStore) {
    UiSettingsBus settingsBus = mock(UiSettingsBus.class);
    when(settingsBus.get()).thenReturn(settings);
    return newStore(new ChatStyles(null), settingsBus, userListStore);
  }

  private static ChatTranscriptStore newStore(
      ChatStyles styles, UiSettingsBus settingsBus, UserListStore userListStore) {
    return new ChatTranscriptStore(
        styles,
        newRenderer(styles),
        null,
        null,
        null,
        null,
        null,
        settingsBus,
        null,
        userListStore);
  }

  private static ChatRichTextRenderer newRenderer(ChatStyles styles) {
    return new ChatRichTextRenderer(null, null, styles, null);
  }

  static UiSettings settingsWithTranscriptCap(int maxLines) {
    return settingsWithTranscriptCap(maxLines, true);
  }

  static UiSettings settingsWithTranscriptCap(
      int maxLines, boolean outgoingDeliveryIndicatorsEnabled) {
    return new UiSettings(
        "darcula",
        "Monospaced",
        12,
        true,
        true,
        false,
        false,
        false,
        true,
        true,
        false,
        true,
        false,
        false,
        true,
        NotificationBackendMode.AUTO,
        true,
        false,
        0,
        0,
        true,
        true,
        false,
        true,
        true,
        true,
        true,
        "dots",
        true,
        true,
        true,
        true,
        true,
        "HH:mm:ss",
        true,
        true,
        100,
        200,
        2000,
        20,
        10,
        6,
        false,
        6,
        18,
        360,
        500,
        maxLines,
        true,
        "#6AA2FF",
        outgoingDeliveryIndicatorsEnabled,
        true,
        true,
        7,
        6,
        30,
        5,
        false,
        15,
        3,
        60,
        5,
        false,
        45,
        120,
        false,
        300,
        2,
        30,
        15,
        MemoryUsageDisplayMode.LONG,
        1000,
        5,
        true,
        false,
        false,
        false,
        List.of(),
        null,
        null,
        false,
        "compact");
  }
}
