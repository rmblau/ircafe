package cafe.woden.ircclient.ui.chat.transcript.message;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;

/** Reaction summary orchestration bound to per-target transcript state. */
public final class ChatTranscriptReactionFlowSupport {

  public record Context(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      Consumer<TargetRef> ensureTargetExists,
      ChatTranscriptReactionSummarySupport reactionSummarySupport) {
    public Context {
      Objects.requireNonNull(docs, "docs");
      Objects.requireNonNull(stateByTarget, "stateByTarget");
      Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
      Objects.requireNonNull(reactionSummarySupport, "reactionSummarySupport");
    }
  }

  public boolean hasReactionFromNick(
      Context context, TargetRef ref, String messageId, String reaction, String nick) {
    if (context == null) return false;
    ChatTranscriptState st = context.stateByTarget().get(ref);
    return context
        .reactionSummarySupport()
        .hasReactionFromNick(st == null ? null : st.reactionSummary(), messageId, reaction, nick);
  }

  public void setReactionChipActionHandler(Context context, ReactionChipActionHandler handler) {
    if (context == null) return;
    Map<TargetRef, ChatTranscriptReactionSummarySupport.State> statesByTarget = new HashMap<>();
    for (Map.Entry<TargetRef, ChatTranscriptState> entry : context.stateByTarget().entrySet()) {
      ChatTranscriptState st = entry.getValue();
      if (st != null) {
        statesByTarget.put(entry.getKey(), st.reactionSummary());
      }
    }
    context.reactionSummarySupport().setReactionChipActionHandler(handler, statesByTarget);
  }

  public void applyMessageReaction(
      Context context,
      TargetRef ref,
      String targetMessageId,
      String reaction,
      String fromNick,
      long tsEpochMs) {
    if (context == null || ref == null) return;
    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    ChatTranscriptState st = context.stateByTarget().get(ref);
    if (doc == null || st == null) return;
    context
        .reactionSummarySupport()
        .applyMessageReaction(
            ref, doc, st.reactionSummary(), targetMessageId, reaction, fromNick, tsEpochMs);
  }

  public void removeMessageReaction(
      Context context,
      TargetRef ref,
      String targetMessageId,
      String reaction,
      String fromNick,
      long tsEpochMs) {
    if (context == null || ref == null) return;
    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    ChatTranscriptState st = context.stateByTarget().get(ref);
    if (doc == null || st == null) return;
    context
        .reactionSummarySupport()
        .removeMessageReaction(
            ref, doc, st.reactionSummary(), targetMessageId, reaction, fromNick, tsEpochMs);
  }
}
