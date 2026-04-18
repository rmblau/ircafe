package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;

final class ChatTranscriptMessageMutationFlowSupport {

  record Context(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      Consumer<TargetRef> ensureTargetExists,
      ChatTranscriptMessageMutationSupport messageMutationSupport) {

    Context {
      docs = Objects.requireNonNull(docs, "docs");
      stateByTarget = Objects.requireNonNull(stateByTarget, "stateByTarget");
      ensureTargetExists = Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
      messageMutationSupport =
          Objects.requireNonNull(messageMutationSupport, "messageMutationSupport");
    }
  }

  boolean applyMessageEdit(
      Context context,
      TargetRef ref,
      String targetMessageId,
      String editedText,
      String fromNick,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags) {
    if (context == null || ref == null) return false;
    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    ChatTranscriptState state = context.stateByTarget().get(ref);
    return context
        .messageMutationSupport()
        .applyMessageEdit(
            ref,
            doc,
            state == null ? null : state.messageCatalog,
            targetMessageId,
            editedText,
            tsEpochMs,
            replacementMessageId,
            replacementIrcv3Tags);
  }

  boolean applyMessageRedaction(
      Context context,
      TargetRef ref,
      String targetMessageId,
      String fromNick,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags) {
    if (context == null || ref == null) return false;
    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    ChatTranscriptState state = context.stateByTarget().get(ref);
    return context
        .messageMutationSupport()
        .applyMessageRedaction(
            ref,
            doc,
            state == null ? null : state.messageCatalog,
            state == null ? null : state.reactionSummary,
            targetMessageId,
            fromNick,
            tsEpochMs,
            replacementMessageId,
            replacementIrcv3Tags);
  }
}
