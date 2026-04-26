package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.flow.ChatTranscriptReactionFlowSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ReactionChipActionHandler;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptReactionFlowSupportTest {

  @Test
  void setReactionChipActionHandlerRebindsExistingStates() {
    ChatTranscriptReactionSummarySupport reactionSummarySupport =
        mock(ChatTranscriptReactionSummarySupport.class);
    ChatTranscriptReactionFlowSupport support = new ChatTranscriptReactionFlowSupport();
    TargetRef ref = new TargetRef("srv", "#chan");
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    Map<TargetRef, ChatTranscriptState> states = new HashMap<>();
    states.put(ref, newState());
    ChatTranscriptReactionFlowSupport.Context context =
        new ChatTranscriptReactionFlowSupport.Context(
            docs, states, r -> {}, reactionSummarySupport);

    ReactionChipActionHandler handler =
        (target, messageId, reactionToken, unreactRequested) -> {};
    support.setReactionChipActionHandler(context, handler);

    verify(reactionSummarySupport).setReactionChipActionHandler(eq(handler), anyMap());
  }

  @Test
  void applyAndRemoveEnsureTargetBeforeDelegating() {
    ChatTranscriptReactionSummarySupport reactionSummarySupport =
        mock(ChatTranscriptReactionSummarySupport.class);
    ChatTranscriptReactionFlowSupport support = new ChatTranscriptReactionFlowSupport();
    TargetRef ref = new TargetRef("srv", "#chan");
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    docs.put(ref, new DefaultStyledDocument());
    Map<TargetRef, ChatTranscriptState> states = new HashMap<>();
    states.put(ref, newState());
    AtomicBoolean ensured = new AtomicBoolean(false);
    ChatTranscriptReactionFlowSupport.Context context =
        new ChatTranscriptReactionFlowSupport.Context(
            docs,
            states,
            r -> {
              if (ref.equals(r)) {
                ensured.set(true);
              }
            },
            reactionSummarySupport);

    support.applyMessageReaction(context, ref, "m-1", ":)", "alice", 10L);
    support.removeMessageReaction(context, ref, "m-1", ":)", "alice", 11L);

    verify(reactionSummarySupport)
        .applyMessageReaction(
            ref, docs.get(ref), states.get(ref).reactionSummary(), "m-1", ":)", "alice", 10L);
    verify(reactionSummarySupport)
        .removeMessageReaction(
            ref, docs.get(ref), states.get(ref).reactionSummary(), "m-1", ":)", "alice", 11L);
    org.junit.jupiter.api.Assertions.assertTrue(ensured.get());
  }

  @Test
  void missingStateReturnsFalseForReactionLookup() {
    ChatTranscriptReactionFlowSupport support = new ChatTranscriptReactionFlowSupport();
    ChatTranscriptReactionSummarySupport reactionSummarySupport =
        mock(ChatTranscriptReactionSummarySupport.class);
    ChatTranscriptReactionFlowSupport.Context context =
        new ChatTranscriptReactionFlowSupport.Context(
            Map.of(), Map.of(), r -> {}, reactionSummarySupport);

    assertFalse(
        support.hasReactionFromNick(context, new TargetRef("srv", "#chan"), "m-1", ":)", "alice"));
  }

  private ChatTranscriptState newState() {
    ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(
            new ChatTranscriptMessageStateSupport.Context(120, "[redacted]", () -> 1L));
    return new ChatTranscriptState(
        messageCatalogSupport.createState(32, 32),
        new ChatTranscriptFilteredLinesSupport.State(),
        new ChatTranscriptPresenceFoldSupport.State());
  }
}
