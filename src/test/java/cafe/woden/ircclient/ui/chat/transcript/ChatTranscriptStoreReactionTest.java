package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreDocumentTestSupport.lineCount;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreDocumentTestSupport.reactionComponent;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreReactionTestSupport.REACTION_MESSAGE_ID;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreReactionTestSupport.THUMBS_UP_REACTION;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreReactionTestSupport.bindReactionChipActionHandler;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreReactionTestSupport.clickFirstReactionChip;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTargetRefTestSupport.channelRef;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTestFactory.newStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.TargetRef;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ChatTranscriptStoreReactionTest {

  @Test
  void removeMessageReactionRemovesRenderedReactionSummaryWhenLastReactionIsCleared() {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "hello", false, 6_000L, "m-42", Map.of("msgid", "m-42"));
    int baseLines = lineCount(store.document(ref));

    store.applyMessageReaction(ref, "m-42", ":+1:", "bob", 6_050L);
    int withReactionLines = lineCount(store.document(ref));

    store.removeMessageReaction(ref, "m-42", ":+1:", "bob", 6_100L);
    int afterRemovalLines = lineCount(store.document(ref));

    assertTrue(withReactionLines > baseLines);
    assertEquals(baseLines, afterRemovalLines);
  }

  @Test
  void hasReactionFromNickMatchesObservedReactorCaseInsensitively() {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "hello", false, 6_000L, "m-42", Map.of("msgid", "m-42"));
    store.applyMessageReaction(ref, "m-42", ":+1:", "Bob", 6_050L);

    assertTrue(store.hasReactionFromNick(ref, "m-42", ":+1:", "bob"));
    assertFalse(store.hasReactionFromNick(ref, "m-42", ":heart:", "bob"));
    assertFalse(store.hasReactionFromNick(ref, "m-42", ":+1:", "carol"));
  }

  @Test
  void reactionChipClickDispatchesConfiguredActionHandler() {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();
    ChatTranscriptStoreReactionTestSupport.ReactionClickCapture clickCapture =
        bindReactionChipActionHandler(store);

    store.appendChatAt(
        ref,
        "alice",
        "hello",
        false,
        6_000L,
        REACTION_MESSAGE_ID,
        Map.of("msgid", REACTION_MESSAGE_ID));
    store.applyMessageReaction(ref, REACTION_MESSAGE_ID, THUMBS_UP_REACTION, "bob", 6_050L);

    assertNotNull(reactionComponent(store.document(ref)));
    clickFirstReactionChip(store.document(ref));

    assertEquals(ref, clickCapture.target());
    assertEquals(REACTION_MESSAGE_ID, clickCapture.messageId());
    assertEquals(THUMBS_UP_REACTION, clickCapture.reaction());
    assertFalse(clickCapture.unreactRequested());
  }

  @Test
  void setReactionChipActionHandlerRebindsExistingReactionChipCallbacks() {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatAt(
        ref,
        "alice",
        "hello",
        false,
        6_000L,
        REACTION_MESSAGE_ID,
        Map.of("msgid", REACTION_MESSAGE_ID));
    store.applyMessageReaction(ref, REACTION_MESSAGE_ID, THUMBS_UP_REACTION, "bob", 6_050L);

    ChatTranscriptStoreReactionTestSupport.ReactionClickCapture clickCapture =
        bindReactionChipActionHandler(store);

    assertNotNull(reactionComponent(store.document(ref)));
    clickFirstReactionChip(store.document(ref));

    assertEquals(ref, clickCapture.target());
    assertEquals(REACTION_MESSAGE_ID, clickCapture.messageId());
    assertEquals(THUMBS_UP_REACTION, clickCapture.reaction());
    assertFalse(clickCapture.unreactRequested());
  }
}
