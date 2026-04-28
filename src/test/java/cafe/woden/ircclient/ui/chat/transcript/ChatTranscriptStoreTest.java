package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTestFactory.newStore;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTestFactory.newStoreWithTranscriptCap;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTestFactory.newStoreWithTranscriptCapAndDeliveryIndicators;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTestFactory.newStoreWithTranscriptCapAndUserList;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreDocumentTestSupport.inlineComponentCount;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreDocumentTestSupport.lineCount;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreDocumentTestSupport.transcriptText;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreDocumentTestSupport.transcriptTextUnchecked;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreMatrixTestSupport.MATRIX_ALICE_USER_ID;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreMatrixTestSupport.MATRIX_BRIDGED_WODENCAFE_USER_ID;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreMatrixTestSupport.MATRIX_SERVER;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreMatrixTestSupport.putMatrixBridgedNick;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreMatrixTestSupport.userListWithMatrixDisplayName;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreManualPreviewTestSupport.newManualPreviewFallbackFixture;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreManualPreviewTestSupport.newStoreWithBlockedImagePreview;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreManualPreviewTestSupport.verifyManualPreviewFallbackAttempted;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTargetRefTestSupport.channelRef;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTargetRefTestSupport.matrixRoomRef;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.roster.UserListStore;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.line.OutgoingSendIndicator;
import cafe.woden.ircclient.ui.util.EmojiFontSupport;
import java.util.Map;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptStoreTest {

  @Test
  void appendChatAtMarksEmojiGlyphRunsInTranscript() throws Exception {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "hello 😀 world", false, 6_500L);

    StyledDocument doc = store.document(ref);
    String text = transcriptText(doc);
    int emojiIndex = text.indexOf("😀");
    assertTrue(emojiIndex >= 0);
    assertTrue(EmojiFontSupport.isEmojiRun(doc.getCharacterElement(emojiIndex).getAttributes()));
    assertFalse(
        EmojiFontSupport.isEmojiRun(
            doc.getCharacterElement(text.indexOf("hello")).getAttributes()));
  }

  @Test
  void appendChatAtTrimsOldestLinesWhenTranscriptCapIsExceeded() throws Exception {
    ChatTranscriptStore store = newStoreWithTranscriptCap(2);
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "line-1", false, 7_000L);
    store.appendChatAt(ref, "alice", "line-2", false, 7_010L);
    store.appendChatAt(ref, "alice", "line-3", false, 7_020L);

    StyledDocument doc = store.document(ref);
    String text = transcriptText(doc);
    assertFalse(text.contains("line-1"));
    assertTrue(text.contains("line-2"));
    assertTrue(text.contains("line-3"));
    assertEquals(2, lineCount(doc));
  }

  @Test
  void transcriptCapZeroDisablesHeadTrimming() throws Exception {
    ChatTranscriptStore store = newStoreWithTranscriptCap(0);
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "line-1", false, 8_000L);
    store.appendChatAt(ref, "alice", "line-2", false, 8_010L);
    store.appendChatAt(ref, "alice", "line-3", false, 8_020L);

    StyledDocument doc = store.document(ref);
    String text = transcriptText(doc);
    assertTrue(text.contains("line-1"));
    assertTrue(text.contains("line-2"));
    assertTrue(text.contains("line-3"));
    assertEquals(3, lineCount(doc));
  }

  @Test
  void readMarkerPersistsWhenSetBeforeUnreadLinesExist() {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.updateReadMarker(ref, 1_000L);
    assertEquals(-1, store.readMarkerJumpOffset(ref));

    store.appendChatAt(ref, "alice", "older", false, 900L);
    assertEquals(-1, store.readMarkerJumpOffset(ref));

    store.appendChatAt(ref, "alice", "newer", false, 1_100L);
    assertTrue(store.readMarkerJumpOffset(ref) >= 0);
  }

  @Test
  void clearReadMarkersForServerRemovesMarkerStateWithoutAffectingOtherServers() {
    ChatTranscriptStore store = newStore();
    TargetRef onServer = channelRef();
    TargetRef otherServer = new TargetRef("other", "#chan");

    store.appendChatAt(onServer, "alice", "older", false, 900L);
    store.appendChatAt(onServer, "alice", "newer", false, 1_100L);
    store.updateReadMarker(onServer, 1_000L);
    assertTrue(store.readMarkerJumpOffset(onServer) >= 0);

    store.appendChatAt(otherServer, "alice", "older", false, 900L);
    store.appendChatAt(otherServer, "alice", "newer", false, 1_100L);
    store.updateReadMarker(otherServer, 1_000L);
    assertTrue(store.readMarkerJumpOffset(otherServer) >= 0);

    store.clearReadMarkersForServer("srv");

    assertEquals(-1, store.readMarkerJumpOffset(onServer));
    assertTrue(store.readMarkerJumpOffset(otherServer) >= 0);

    store.appendChatAt(onServer, "alice", "latest", false, 1_200L);
    assertEquals(-1, store.readMarkerJumpOffset(onServer));
  }

  @Test
  void appendChatAtAddsManualPreviewMarkerForPolicyBlockedUrls() throws Exception {
    ChatTranscriptStore store = newStoreWithBlockedImagePreview("https://blocked.example/a.png");
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "https://blocked.example/a.png", false, 9_000L);

    StyledDocument doc = store.document(ref);
    String text = transcriptText(doc);
    int marker = text.indexOf("👁");
    assertTrue(marker >= 0);
    Object markerUrl =
        doc.getCharacterElement(marker)
            .getAttributes()
            .getAttribute(ChatStyles.ATTR_MANUAL_PREVIEW_URL);
    assertEquals("https://blocked.example/a.png", markerUrl);
  }

  @Test
  void insertManualPreviewAtFallsBackToLinkPreviewWhenImageInsertDeclines() {
    ChatTranscriptStoreManualPreviewTestSupport.ManualPreviewFallbackFixture fixture =
        newManualPreviewFallbackFixture();
    ChatTranscriptStore store = fixture.store();
    TargetRef ref = channelRef();
    store.appendChat(ref, "alice", "line");

    assertTrue(store.insertManualPreviewAt(ref, 0, "https://example.com/x"));
    verifyManualPreviewFallbackAttempted(fixture);
  }

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

  @Test
  void appendChatAtRendersMatrixDisplayNameInCompactModeAndPreservesRawMetaFrom() throws Exception {
    UserListStore userListStore = userListWithMatrixDisplayName(MATRIX_ALICE_USER_ID, "Alice");
    TargetRef ref = matrixRoomRef();

    ChatTranscriptStore store = newStoreWithTranscriptCapAndUserList(0, userListStore);
    store.appendChatAt(
        ref,
        MATRIX_ALICE_USER_ID,
        "hello matrix",
        false,
        11_000L,
        "m-1",
        Map.of("msgid", "m-1"));

    StyledDocument doc = store.document(ref);
    String text = transcriptText(doc);
    assertTrue(text.contains("Alice: hello matrix"));
    assertFalse(text.contains(MATRIX_ALICE_USER_ID + ": hello matrix"));

    Element firstLine = doc.getDefaultRootElement().getElement(0);
    Object metaFrom =
        doc.getCharacterElement(firstLine.getStartOffset())
            .getAttributes()
            .getAttribute(ChatStyles.ATTR_META_FROM);
    assertEquals(MATRIX_ALICE_USER_ID, String.valueOf(metaFrom));
  }

  @Test
  void appendChatAtRendersMatrixDisplayNameFromRosterSnapshotWithoutSetnameEvents()
      throws Exception {
    UserListStore userListStore = new UserListStore();
    TargetRef ref = matrixRoomRef();
    putMatrixBridgedNick(userListStore, MATRIX_BRIDGED_WODENCAFE_USER_ID, "wodencafe");

    ChatTranscriptStore store = newStoreWithTranscriptCapAndUserList(0, userListStore);
    store.appendChatAt(
        ref,
        MATRIX_BRIDGED_WODENCAFE_USER_ID,
        "hi all",
        false,
        12_000L,
        "m-2",
        Map.of("msgid", "m-2"));

    String text = transcriptText(store.document(ref));
    assertTrue(text.contains("wodencafe: hi all"));
    assertFalse(text.contains(MATRIX_BRIDGED_WODENCAFE_USER_ID + ": hi all"));
  }

  @Test
  void refreshMatrixDisplayNamesRelabelsExistingHistoryLinesAfterRosterSnapshotArrives()
      throws Exception {
    UserListStore userListStore = new UserListStore();
    TargetRef ref = matrixRoomRef();
    ChatTranscriptStore store = newStoreWithTranscriptCapAndUserList(0, userListStore);

    store.appendChatFromHistory(
        ref,
        MATRIX_BRIDGED_WODENCAFE_USER_ID,
        "hi from local scrollback",
        false,
        12_500L,
        "m-refresh",
        Map.of("msgid", "m-refresh"));

    String before = transcriptText(store.document(ref));
    assertTrue(before.contains(MATRIX_BRIDGED_WODENCAFE_USER_ID + ": hi from local scrollback"));

    putMatrixBridgedNick(userListStore, MATRIX_BRIDGED_WODENCAFE_USER_ID, "wodencafe");

    assertEquals(1, store.refreshMatrixDisplayNames(ref));

    String after = transcriptText(store.document(ref));
    assertTrue(after.contains("wodencafe: hi from local scrollback"));
    assertFalse(after.contains(MATRIX_BRIDGED_WODENCAFE_USER_ID + ": hi from local scrollback"));
    assertEquals(0, store.refreshMatrixDisplayNames(ref));
  }

  @Test
  void refreshMatrixDisplayNameAcrossServerRelabelsOnlyMatchingMatrixUserId() throws Exception {
    UserListStore userListStore = new UserListStore();
    ChatTranscriptStore store = newStoreWithTranscriptCapAndUserList(0, userListStore);
    TargetRef roomA = new TargetRef(MATRIX_SERVER, "#room-a:matrix.example.org");
    TargetRef roomB = new TargetRef(MATRIX_SERVER, "#room-b:matrix.example.org");
    TargetRef otherServer = new TargetRef("other", "#room:other.example.org");

    store.appendChatFromHistory(
        roomA,
        MATRIX_ALICE_USER_ID,
        "hello a",
        false,
        1_000L,
        "m-a",
        Map.of("msgid", "m-a"));
    store.appendChatFromHistory(
        roomB,
        MATRIX_ALICE_USER_ID,
        "hello b",
        false,
        1_100L,
        "m-b",
        Map.of("msgid", "m-b"));
    store.appendChatFromHistory(
        roomB,
        "@bob:matrix.example.org",
        "hello bob",
        false,
        1_200L,
        "m-bob",
        Map.of("msgid", "m-bob"));
    store.appendChatFromHistory(
        otherServer,
        MATRIX_ALICE_USER_ID,
        "hello other",
        false,
        1_300L,
        "m-other",
        Map.of("msgid", "m-other"));

    userListStore.updateRealNameAcrossChannels(MATRIX_SERVER, MATRIX_ALICE_USER_ID, "Alice");

    int changed = store.refreshMatrixDisplayNameAcrossServer(MATRIX_SERVER, MATRIX_ALICE_USER_ID);
    assertEquals(2, changed);

    String textA = transcriptText(store.document(roomA));
    assertTrue(textA.contains("Alice: hello a"));
    assertFalse(textA.contains(MATRIX_ALICE_USER_ID + ": hello a"));

    String textB = transcriptText(store.document(roomB));
    assertTrue(textB.contains("Alice: hello b"));
    assertTrue(textB.contains("@bob:matrix.example.org: hello bob"));

    String textOther = transcriptText(store.document(otherServer));
    assertTrue(textOther.contains(MATRIX_ALICE_USER_ID + ": hello other"));
  }

}
