package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.irc.roster.UserListPort;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.chat.embed.ChatImageEmbedder;
import cafe.woden.ircclient.ui.chat.embed.ChatLinkPreviewEmbedder;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageInteractionCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageLineCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptPlainSpoilerCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReplyFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMatrixDisplayNameCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;

/** Wires the transcript helper graph used by {@link ChatTranscriptStore}. */
public final class ChatTranscriptStoreComposition {

  public record Components(
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator,
      ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator,
      ChatTranscriptReplyFlowCoordinator replyFlowCoordinator,
      ChatTranscriptMessageInteractionCoordinator messageInteractionCoordinator,
      ChatTranscriptMessageLineCoordinator messageLineCoordinator,
      ChatTranscriptPlainSpoilerCoordinator plainSpoilerCoordinator,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator) {}

  private ChatTranscriptStoreComposition() {}

  public static Components create(
      ChatTranscriptStore store,
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      ChatTimestampFormatter ts,
      NickColorService nickColors,
      NickColorSettingsBus nickColorSettings,
      ChatImageEmbedder imageEmbeds,
      ChatLinkPreviewEmbedder linkPreviews,
      UiSettingsBus uiSettings,
      FilterEngine filterEngine,
      UserListPort userListStore) {

    ChatTranscriptRuntimeComposition.Components runtimeComposition =
        ChatTranscriptRuntimeComposition.create(
            store, styles, nickColors, nickColorSettings, uiSettings);
    ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator =
        runtimeComposition.runtimeFlowCoordinator();
    ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport =
        runtimeComposition.runtimeSettingsSupport();
    ChatTranscriptStyleRoutingSupport styleRoutingSupport =
        runtimeComposition.styleRoutingSupport();
    ChatTranscriptLineCapSupport lineCapSupport = runtimeComposition.lineCapSupport();
    ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        runtimeComposition.messageCatalogSupport();
    ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator =
        runtimeComposition.targetRuntimeCoordinator();

    ChatTranscriptLineComposition.Components lineComposition =
        ChatTranscriptLineComposition.create(
            styles, renderer, ts, styleRoutingSupport, runtimeFlowCoordinator, lineCapSupport);
    ChatTranscriptDocumentLineSupport documentLineSupport = lineComposition.documentLineSupport();
    ChatTranscriptFilterComposition.Components filterComposition =
        ChatTranscriptFilterComposition.create(
            styles,
            filterEngine,
            styleRoutingSupport,
            documentLineSupport,
            runtimeFlowCoordinator,
            lineCapSupport,
            targetRuntimeCoordinator,
            runtimeSettingsSupport);
    ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator =
        filterComposition.filteredFlowCoordinator();
    ChatTranscriptFilterRoutingSupport filterRoutingSupport =
        filterComposition.filterRoutingSupport();
    ChatTranscriptPresenceFoldSupport presenceFoldSupport = lineComposition.presenceFoldSupport();
    ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator =
        new ChatTranscriptMatrixDisplayNameCoordinator(
            uiSettings, userListStore, targetRuntimeCoordinator.docs());
    ChatTranscriptSpoilerComposition.Components spoilerComposition =
        ChatTranscriptSpoilerComposition.create(
            store,
            styles,
            renderer,
            ts,
            nickColors,
            uiSettings,
            matrixDisplayNameCoordinator,
            documentLineSupport,
            styleRoutingSupport,
            filterRoutingSupport,
            runtimeFlowCoordinator,
            lineCapSupport,
            targetRuntimeCoordinator,
            runtimeSettingsSupport,
            filteredFlowCoordinator);
    ChatTranscriptPlainSpoilerCoordinator plainSpoilerCoordinator =
        spoilerComposition.plainSpoilerCoordinator();
    ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport =
        lineComposition.auxiliaryRowsSupport();
    ChatTranscriptMessageComposition.Components messageComposition =
        ChatTranscriptMessageComposition.create(
            store,
            styles,
            renderer,
            ts,
            nickColors,
            imageEmbeds,
            linkPreviews,
            matrixDisplayNameCoordinator,
            styleRoutingSupport,
            filterRoutingSupport,
            documentLineSupport,
            lineCapSupport,
            runtimeFlowCoordinator,
            targetRuntimeCoordinator,
            runtimeSettingsSupport,
            messageCatalogSupport,
            filteredFlowCoordinator);
    ChatTranscriptReplyFlowCoordinator replyFlowCoordinator =
        messageComposition.replyFlowCoordinator();
    ChatTranscriptMessageLineCoordinator messageLineCoordinator =
        messageComposition.messageLineCoordinator();
    ChatTranscriptMessageInteractionCoordinator messageInteractionCoordinator =
        messageComposition.messageInteractionCoordinator();
    ChatTranscriptRuntimeContextBinding.bind(
        runtimeFlowCoordinator,
        presenceFoldSupport,
        filterRoutingSupport,
        filteredFlowCoordinator,
        runtimeSettingsSupport,
        targetRuntimeCoordinator,
        documentLineSupport,
        messageLineCoordinator,
        auxiliaryRowsSupport);
    return new Components(
        filteredFlowCoordinator,
        matrixDisplayNameCoordinator,
        replyFlowCoordinator,
        messageInteractionCoordinator,
        messageLineCoordinator,
        plainSpoilerCoordinator,
        runtimeFlowCoordinator,
        targetRuntimeCoordinator);
  }
}
