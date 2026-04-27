package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.logging.history.LoadOlderControlState;
import cafe.woden.ircclient.model.TargetRef;
import java.util.function.BooleanSupplier;

/** Adapts history load-older controls onto the transcript store UI control API. */
final class ChatHistoryTranscriptLoadOlderControlAdapter {

  private final ChatTranscriptStore transcripts;

  ChatHistoryTranscriptLoadOlderControlAdapter(ChatTranscriptStore transcripts) {
    this.transcripts = transcripts;
  }

  java.awt.Component ensure(TargetRef ref) {
    return transcripts.ensureLoadOlderMessagesControl(ref);
  }

  void setState(TargetRef ref, LoadOlderControlState state) {
    transcripts.setLoadOlderMessagesControlState(
        ref, ChatHistoryTranscriptLoadOlderStateMapper.toUiState(state));
  }

  void setHandler(TargetRef ref, BooleanSupplier onLoad) {
    transcripts.setLoadOlderMessagesControlHandler(ref, onLoad);
  }
}
