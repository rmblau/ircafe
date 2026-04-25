package cafe.woden.ircclient.ui.chat.transcript.flow;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReplyContextSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;

/** Coordinates reply-context follow-up rendering for live appended messages. */
public final class ChatTranscriptReplyFlowSupport {

  public record Context(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      Consumer<TargetRef> ensureTargetExists,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptReplyContextSupport.Context replyContextSupportContext,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport) {
    public Context {
      Objects.requireNonNull(docs, "docs");
      Objects.requireNonNull(stateByTarget, "stateByTarget");
      Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
      Objects.requireNonNull(documentLineSupport, "documentLineSupport");
      Objects.requireNonNull(replyContextSupportContext, "replyContextSupportContext");
      Objects.requireNonNull(messageCatalogSupport, "messageCatalogSupport");
    }
  }

  public void appendReplyContextLine(
      Context context, TargetRef ref, String fromNick, String replyToMsgId, long tsEpochMs) {
    if (context == null) return;
    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    ChatTranscriptState state = context.stateByTarget().get(ref);
    if (doc == null) return;

    context.documentLineSupport().ensureAtLineStart(doc);
    ChatTranscriptReplyContextSupport.appendReplyContextLine(
        context.replyContextSupportContext(),
        doc,
        ref,
        fromNick,
        replyToMsgId,
        tsEpochMs,
        messageId -> previewForMessageId(context, state, messageId));
  }

  private String previewForMessageId(Context context, ChatTranscriptState state, String messageId) {
    return context
        .messageCatalogSupport()
        .previewForMessageId(state == null ? null : state.messageCatalog(), messageId);
  }
}
