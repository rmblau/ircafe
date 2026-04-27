package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRestyleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;

/** Builds target-scoped runtime state collaborators for transcript composition. */
final class ChatTranscriptTargetRuntimeComposition {

  private static final int REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET = 512;
  private static final int REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET = 512;

  private ChatTranscriptTargetRuntimeComposition() {}

  static ChatTranscriptTargetRuntimeCoordinator create(
      ChatTranscriptStore store,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      ChatTranscriptRestyleSupport.Context restyleSupportContext,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      NickColorSettingsBus nickColorSettings) {
    return new ChatTranscriptTargetRuntimeCoordinator(
        () ->
            messageCatalogSupport.createState(
                REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET, REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET),
        store,
        180,
        restyleSupportContext,
        runtimeSettingsSupport::safeSettings,
        runtimeSettingsSupport::configuredOutgoingLineColor,
        nickColorSettings);
  }
}
