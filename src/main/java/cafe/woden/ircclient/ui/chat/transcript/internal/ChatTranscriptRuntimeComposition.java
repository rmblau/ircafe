package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRestyleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;

/** Builds runtime and target-state collaborators for the transcript store composition. */
final class ChatTranscriptRuntimeComposition {

  private static final int REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET = 512;
  private static final int REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET = 512;
  private static final int REPLY_PREVIEW_TEXT_MAX_CHARS = 120;
  private static final String REDACTED_MESSAGE_PLACEHOLDER = "[message redacted]";

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
    ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport =
        new ChatTranscriptRuntimeSettingsSupport(uiSettings, styles);
    ChatTranscriptStyleRoutingSupport styleRoutingSupport =
        new ChatTranscriptStyleRoutingSupport(
            styles,
            runtimeSettingsSupport::safeSettings,
            runtimeSettingsSupport::configuredOutgoingLineColor);
    ChatTranscriptLineCapSupport lineCapSupport =
        new ChatTranscriptLineCapSupport(
            runtimeSettingsSupport::transcriptMaxLinesPerTarget,
            runtimeFlowCoordinator::resetAfterHeadTrim,
            ref -> runtimeFlowCoordinator.maybeRenderPendingReadMarker(ref, null));
    ChatTranscriptRestyleSupport.Context restyleSupportContext =
        new ChatTranscriptRestyleSupport.Context(
            styles, nickColors, styleRoutingSupport::applyFilterActionStyle);
    ChatTranscriptMessageStateSupport.Context messageStateSupportContext =
        new ChatTranscriptMessageStateSupport.Context(
            REPLY_PREVIEW_TEXT_MAX_CHARS, REDACTED_MESSAGE_PLACEHOLDER, System::currentTimeMillis);
    ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(messageStateSupportContext);
    ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator =
        new ChatTranscriptTargetRuntimeCoordinator(
            () ->
                messageCatalogSupport.createState(
                    REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET, REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET),
            store,
            180,
            restyleSupportContext,
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
