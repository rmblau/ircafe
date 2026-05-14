package cafe.woden.ircclient.ui.chat.transcript.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.irc.roster.UserListStore;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import cafe.woden.ircclient.ui.settings.UiSettingsTestFixtures;

/** Test factory helpers for {@link ChatTranscriptStore}. */
public final class ChatTranscriptStoreTestFactory {

  private ChatTranscriptStoreTestFactory() {}

  public static ChatTranscriptStore newStore() {
    return newStore(new ChatStyles(null), null, null);
  }

  public static ChatTranscriptStore newStoreWithTranscriptCap(int maxLines) {
    return newStoreWithSettings(settingsWithTranscriptCap(maxLines), null);
  }

  public static ChatTranscriptStore newStoreWithTranscriptCapAndDeliveryIndicators(
      int maxLines, boolean enabled) {
    return newStoreWithSettings(settingsWithTranscriptCap(maxLines, enabled), null);
  }

  public static ChatTranscriptStore newStoreWithTranscriptCapAndUserList(
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

  public static UiSettings settingsWithTranscriptCap(int maxLines) {
    return settingsWithTranscriptCap(maxLines, true);
  }

  public static UiSettings settingsWithTranscriptCap(
      int maxLines, boolean outgoingDeliveryIndicatorsEnabled) {
    return UiSettingsTestFixtures.builder()
        .chatTranscriptMaxLinesPerTarget(maxLines)
        .outgoingDeliveryIndicatorsEnabled(outgoingDeliveryIndicatorsEnabled)
        .build();
  }
}
