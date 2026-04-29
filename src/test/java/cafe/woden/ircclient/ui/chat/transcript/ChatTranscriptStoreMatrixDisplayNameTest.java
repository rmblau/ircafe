package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreMatrixTestSupport.MATRIX_ALICE_USER_ID;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreMatrixTestSupport.MATRIX_BRIDGED_WODENCAFE_USER_ID;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreMatrixTestSupport.MATRIX_SERVER;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreMatrixTestSupport.putMatrixBridgedNick;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreMatrixTestSupport.userListWithMatrixDisplayName;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreTargetRefTestSupport.matrixRoomRef;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTestFactory.newStoreWithTranscriptCapAndUserList;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreDocumentTestSupport.transcriptText;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.roster.UserListStore;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import java.util.Map;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptStoreMatrixDisplayNameTest {

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
