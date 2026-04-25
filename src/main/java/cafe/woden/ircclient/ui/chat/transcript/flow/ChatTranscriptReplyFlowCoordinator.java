package cafe.woden.ircclient.ui.chat.transcript.flow;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReplyContextSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;

/** Wraps reply-context transcript follow-up rendering behind a stable callback surface. */
public final class ChatTranscriptReplyFlowCoordinator {

  private final ChatTranscriptReplyFlowSupport support = new ChatTranscriptReplyFlowSupport();
  private final ChatTranscriptReplyFlowSupport.Context context;

  public ChatTranscriptReplyFlowCoordinator(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      Consumer<TargetRef> ensureTargetExists,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptReplyContextSupport.Context replyContextSupportContext,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport) {
    this.context =
        new ChatTranscriptReplyFlowSupport.Context(
            docs,
            stateByTarget,
            ensureTargetExists,
            documentLineSupport,
            replyContextSupportContext,
            messageCatalogSupport);
  }

  public void appendReplyContextLine(
      TargetRef ref, String fromNick, String replyToMsgId, long tsEpochMs) {
    support.appendReplyContextLine(context, ref, fromNick, replyToMsgId, tsEpochMs);
  }
}
