package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreTargetRefTestSupport.channelRef;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreTestFactory.newStoreWithTranscriptCapAndDeliveryIndicators;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreDocumentTestSupport.inlineComponentCount;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreDocumentTestSupport.transcriptTextUnchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.line.OutgoingSendIndicator;
import java.util.Map;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptStorePendingOutgoingTest {

  @Test
  void appendPendingOutgoingChatSkipsSpinnerWhenDeliveryIndicatorsAreDisabled() {
    ChatTranscriptStore store = newStoreWithTranscriptCapAndDeliveryIndicators(0, false);
    TargetRef ref = channelRef();

    store.appendPendingOutgoingChat(ref, "pending-1", "me", "hello", 10_000L);

    StyledDocument doc = store.document(ref);
    assertTrue(transcriptTextUnchecked(doc).contains("hello"));
    assertEquals(0, inlineComponentCount(doc, OutgoingSendIndicator.PendingSpinner.class));
  }

  @Test
  void resolvePendingOutgoingChatSkipsConfirmedDotWhenDeliveryIndicatorsAreDisabled() {
    ChatTranscriptStore store = newStoreWithTranscriptCapAndDeliveryIndicators(0, false);
    TargetRef ref = channelRef();

    store.appendPendingOutgoingChat(ref, "pending-2", "me", "hello", 10_000L);
    boolean resolved =
        store.resolvePendingOutgoingChat(
            ref, "pending-2", "me", "hello", 10_100L, "msg-1", Map.of("msgid", "msg-1"));

    assertTrue(resolved);
    StyledDocument doc = store.document(ref);
    assertTrue(transcriptTextUnchecked(doc).contains("hello"));
    assertEquals(0, inlineComponentCount(doc, OutgoingSendIndicator.ConfirmedDot.class));
  }

  @Test
  void resolvePendingOutgoingChatAddsConfirmedDotWhenDeliveryIndicatorsAreEnabled() {
    ChatTranscriptStore store = newStoreWithTranscriptCapAndDeliveryIndicators(0, true);
    TargetRef ref = channelRef();

    store.appendPendingOutgoingChat(ref, "pending-2", "me", "hello", 10_000L);
    boolean resolved =
        store.resolvePendingOutgoingChat(
            ref, "pending-2", "me", "hello", 10_100L, "msg-1", Map.of("msgid", "msg-1"));

    assertTrue(resolved);
    StyledDocument doc = store.document(ref);
    assertTrue(transcriptTextUnchecked(doc).contains("hello"));
    assertEquals(1, inlineComponentCount(doc, OutgoingSendIndicator.ConfirmedDot.class));
  }

  @Test
  void resolvePendingOutgoingChatAppliesReplyReactionToReferencedMessage() {
    ChatTranscriptStore store = newStoreWithTranscriptCapAndDeliveryIndicators(0, false);
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "hello", false, 9_000L, "m-1", Map.of("msgid", "m-1"));
    store.appendPendingOutgoingChat(ref, "pending-4", "bob", "react", 9_100L);

    boolean resolved =
        store.resolvePendingOutgoingChat(
            ref,
            "pending-4",
            "bob",
            "react",
            9_200L,
            "m-2",
            Map.of("msgid", "m-2", "draft/reply", "m-1", "draft/react", ":+1:"));

    assertTrue(resolved);
    assertTrue(store.hasReactionFromNick(ref, "m-1", ":+1:", "bob"));
  }

  @Test
  void failPendingOutgoingChatReplacesSpinnerLineWithFailedSuffix() {
    ChatTranscriptStore store = newStoreWithTranscriptCapAndDeliveryIndicators(0, true);
    TargetRef ref = channelRef();

    store.appendPendingOutgoingChat(ref, "pending-3", "me", "hello", 10_000L);

    boolean failed =
        store.failPendingOutgoingChat(ref, "pending-3", "me", "hello", 10_100L, "network");

    assertTrue(failed);
    StyledDocument doc = store.document(ref);
    assertTrue(transcriptTextUnchecked(doc).contains("hello [failed: network]"));
    assertEquals(0, inlineComponentCount(doc, OutgoingSendIndicator.PendingSpinner.class));
  }
}
