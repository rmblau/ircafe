package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;

/** Builds runtime and target-state collaborators for the transcript store composition. */
final class ChatTranscriptRuntimeComposition {

  private static final int REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET = 512;
  private static final int REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET = 512;

  record Components(
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptLineCapSupport lineCapSupport,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator) {}

  private ChatTranscriptRuntimeComposition() {}

  static Components create(
      ChatTranscriptStore store,
      ChatStyles styles,
      NickColorService nickColors,
      NickColorSettingsBus nickColorSettings,
      UiSettingsBus uiSettings) {
    ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator =
        new ChatTranscriptRuntimeFlowCoordinator(store, styles);
    ChatTranscriptRuntimeSupportComposition.Components runtimeSupportComposition =
        ChatTranscriptRuntimeSupportComposition.create(
            styles, nickColors, uiSettings, runtimeFlowCoordinator);
    ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport =
        runtimeSupportComposition.runtimeSettingsSupport();
    ChatTranscriptStyleRoutingSupport styleRoutingSupport =
        runtimeSupportComposition.styleRoutingSupport();
    ChatTranscriptLineCapSupport lineCapSupport = runtimeSupportComposition.lineCapSupport();
    ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        runtimeSupportComposition.messageCatalogSupport();
    ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator =
        new ChatTranscriptTargetRuntimeCoordinator(
            () ->
                messageCatalogSupport.createState(
                    REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET, REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET),
            store,
            180,
            runtimeSupportComposition.restyleSupportContext(),
            runtimeSettingsSupport::safeSettings,
            runtimeSettingsSupport::configuredOutgoingLineColor,
            nickColorSettings);
    return new Components(
        runtimeFlowCoordinator,
        runtimeSettingsSupport,
        styleRoutingSupport,
        lineCapSupport,
        messageCatalogSupport,
        targetRuntimeCoordinator);
  }
}
