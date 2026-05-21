package cafe.woden.ircclient.ui.chat.transcript.spoiler;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;
import java.util.Objects;

/** Owns plain-text append and spoiler append/history flow behind one delegate surface. */
public final class ChatTranscriptPlainSpoilerCoordinator {

  private final ChatTranscriptPlainAppendSupport.Context plainAppendContext;
  private final ChatTranscriptSpoilerFlowSupport.Context spoilerFlowContext;

  public ChatTranscriptPlainSpoilerCoordinator(
      ChatTranscriptPlainAppendSupport.Context plainAppendContext,
      ChatTranscriptSpoilerFlowSupport.Context spoilerFlowContext) {
    this.plainAppendContext = Objects.requireNonNull(plainAppendContext, "plainAppendContext");
    this.spoilerFlowContext = Objects.requireNonNull(spoilerFlowContext, "spoilerFlowContext");
  }

  public void appendPlain(TargetRef ref, String text) {
    ChatTranscriptPlainAppendSupport.appendPlain(plainAppendContext, ref, text);
  }

  public void appendSpoilerChat(TargetRef ref, String fromNick, String text) {
    appendSpoiler(ref, fromNick, text, null);
  }

  public void appendSpoilerChatFromHistory(
      TargetRef ref, String fromNick, String text, long tsEpochMs) {
    appendSpoiler(ref, fromNick, text, tsEpochMs);
  }

  public int insertSpoilerChatFromHistoryAt(
      TargetRef ref, int insertAt, String fromNick, String text, long tsEpochMs) {
    return ChatTranscriptSpoilerFlowSupport.insertSpoilerFromHistory(
        spoilerFlowContext, ref, insertAt, fromNick, text, tsEpochMs);
  }

  private void appendSpoiler(TargetRef ref, String fromNick, String text, Long tsEpochMs) {
    ChatTranscriptSpoilerFlowSupport.appendSpoiler(
        spoilerFlowContext, ref, fromNick, text, tsEpochMs);
  }
}
