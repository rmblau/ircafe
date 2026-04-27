package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;

/** Adapts history insert batch and divider operations onto the transcript store API. */
final class ChatHistoryTranscriptBatchAdapter {

  private final ChatTranscriptStore transcripts;

  ChatHistoryTranscriptBatchAdapter(ChatTranscriptStore transcripts) {
    this.transcripts = transcripts;
  }

  void begin(TargetRef ref) {
    transcripts.beginHistoryInsertBatch(ref);
  }

  void begin(TargetRef ref, boolean forceDeferRichText) {
    transcripts.beginHistoryInsertBatch(ref, forceDeferRichText);
  }

  void end(TargetRef ref) {
    transcripts.endHistoryInsertBatch(ref);
  }

  int loadOlderInsertOffset(TargetRef ref) {
    return transcripts.loadOlderInsertOffset(ref);
  }

  boolean hasContentAfterOffset(TargetRef ref, int offset) {
    return transcripts.hasContentAfterOffset(ref, offset);
  }

  void ensureHistoryDivider(TargetRef ref, int insertAt, String labelText) {
    transcripts.ensureHistoryDivider(ref, insertAt, labelText);
  }

  void markHistoryDividerPending(TargetRef ref, String labelText) {
    transcripts.markHistoryDividerPending(ref, labelText);
  }
}
