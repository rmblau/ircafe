package cafe.woden.ircclient.ui.chat.transcript.history;

import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;

/** Groups history message append/insert adapters. */
record ChatHistoryTranscriptMessageAdapters(
    ChatHistoryTranscriptInsertAdapter insert, ChatHistoryTranscriptAppendAdapter append) {

  static ChatHistoryTranscriptMessageAdapters create(ChatTranscriptStore transcripts) {
    return new ChatHistoryTranscriptMessageAdapters(
        new ChatHistoryTranscriptInsertAdapter(transcripts),
        new ChatHistoryTranscriptAppendAdapter(transcripts));
  }
}
