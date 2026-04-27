package cafe.woden.ircclient.ui.chat.transcript.history;

import cafe.woden.ircclient.config.api.UiSettingsRuntimeConfigPort;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;

/** Groups the small transcript history adapters used by the history port adapter. */
record ChatHistoryTranscriptAdapters(
    ChatHistoryTranscriptDocumentAdapter document,
    ChatHistoryTranscriptLoadOlderControlAdapter loadOlderControls,
    ChatHistoryTranscriptBatchAdapter batch,
    ChatHistoryTranscriptMessageAdapters messages,
    ChatHistoryTranscriptSettingsReader settings) {

  static ChatHistoryTranscriptAdapters create(
      ChatTranscriptStore transcripts,
      UiSettingsBus settingsBus,
      UiSettingsRuntimeConfigPort runtimeConfig) {
    return new ChatHistoryTranscriptAdapters(
        new ChatHistoryTranscriptDocumentAdapter(transcripts),
        new ChatHistoryTranscriptLoadOlderControlAdapter(transcripts),
        new ChatHistoryTranscriptBatchAdapter(transcripts),
        ChatHistoryTranscriptMessageAdapters.create(transcripts),
        new ChatHistoryTranscriptSettingsReader(settingsBus, runtimeConfig));
  }
}
