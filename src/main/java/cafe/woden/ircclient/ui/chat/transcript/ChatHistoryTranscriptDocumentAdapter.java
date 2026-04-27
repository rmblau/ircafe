package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;
import java.util.OptionalLong;
import javax.swing.text.StyledDocument;

/** Adapts history transcript document queries onto the transcript store API. */
final class ChatHistoryTranscriptDocumentAdapter {

  private final ChatTranscriptStore transcripts;

  ChatHistoryTranscriptDocumentAdapter(ChatTranscriptStore transcripts) {
    this.transcripts = transcripts;
  }

  StyledDocument document(TargetRef ref) {
    return transcripts.document(ref);
  }

  OptionalLong earliestTimestampEpochMs(TargetRef ref) {
    return transcripts.earliestTimestampEpochMs(ref);
  }
}
