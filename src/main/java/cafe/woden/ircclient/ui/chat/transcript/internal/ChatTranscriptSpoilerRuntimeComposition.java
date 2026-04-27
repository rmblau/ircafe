package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMatrixDisplayNameCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerComponentSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerRevealSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerRuntimeSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerWriteSupport;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;

/** Builds spoiler rendering and runtime contexts for spoiler support composition. */
final class ChatTranscriptSpoilerRuntimeComposition {

  record Components(
      ChatTranscriptSpoilerWriteSupport.Context spoilerWriteSupportContext,
      ChatTranscriptSpoilerRuntimeSupport.Context spoilerRuntimeSupportContext) {}

  private ChatTranscriptSpoilerRuntimeComposition() {}

  static Components create(
      ChatTranscriptStore store,
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      ChatTimestampFormatter ts,
      NickColorService nickColors,
      UiSettingsBus uiSettings,
      ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport) {
    ChatTranscriptSpoilerComponentSupport.Context spoilerComponentSupportContext =
        new ChatTranscriptSpoilerComponentSupport.Context(
            uiSettings, nickColors, matrixDisplayNameCoordinator::renderTranscriptFrom);
    ChatTranscriptSpoilerWriteSupport.Context spoilerWriteSupportContext =
        new ChatTranscriptSpoilerWriteSupport.Context(
            styles, spoilerComponentSupportContext, styleRoutingSupport::withFilterMatch);
    ChatTranscriptSpoilerRevealSupport.Context spoilerRevealSupportContext =
        new ChatTranscriptSpoilerRevealSupport.Context(
            styles, renderer, nickColors, matrixDisplayNameCoordinator::renderTranscriptFrom);
    ChatTranscriptSpoilerRuntimeSupport.Context spoilerRuntimeSupportContext =
        new ChatTranscriptSpoilerRuntimeSupport.Context(
            ts,
            runtimeSettingsSupport::timestampsIncludeChatMessages,
            spoilerRevealSupportContext,
            store);
    return new Components(spoilerWriteSupportContext, spoilerRuntimeSupportContext);
  }
}
