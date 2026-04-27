package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRestyleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;

/** Builds reusable runtime support collaborators for transcript composition. */
final class ChatTranscriptRuntimeSupportComposition {

  private static final int REPLY_PREVIEW_TEXT_MAX_CHARS = 120;
  private static final String REDACTED_MESSAGE_PLACEHOLDER = "[message redacted]";

  record Components(
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptLineCapSupport lineCapSupport,
      ChatTranscriptRestyleSupport.Context restyleSupportContext,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport) {}

  private ChatTranscriptRuntimeSupportComposition() {}

  static Components create(
      ChatStyles styles,
      NickColorService nickColors,
      UiSettingsBus uiSettings,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator) {
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
    return new Components(
        runtimeSettingsSupport,
        styleRoutingSupport,
        lineCapSupport,
        restyleSupportContext,
        messageCatalogSupport);
  }
}
