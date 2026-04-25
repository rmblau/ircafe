package cafe.woden.ircclient.ui.chat.transcript.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.roster.UserListStore;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Test;

class ChatTranscriptMatrixDisplayNameCoordinatorTest {

  @Test
  void refreshMatrixDisplayNamesUsesCoordinatorDocumentMap() throws Exception {
    UserListStore userListStore = new UserListStore();
    userListStore.updateRealNameAcrossChannels("matrix", "@alice:matrix.org", "Alice");
    TargetRef ref = new TargetRef("matrix", "#room:matrix.org");
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SimpleAttributeSet attrs = new SimpleAttributeSet();
    attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_FROM);
    attrs.addAttribute(ChatStyles.ATTR_META_FROM, "@alice:matrix.org");
    doc.insertString(0, "@alice:matrix.org: ", attrs);
    doc.insertString(doc.getLength(), "hello\n", new SimpleAttributeSet());
    Map<TargetRef, javax.swing.text.StyledDocument> docs = new HashMap<>();
    docs.put(ref, doc);
    ChatTranscriptMatrixDisplayNameCoordinator coordinator =
        new ChatTranscriptMatrixDisplayNameCoordinator(null, userListStore, docs);

    int changed = coordinator.refreshMatrixDisplayNames(ref);

    assertEquals(1, changed);
    assertEquals("Alice", coordinator.renderTranscriptFrom(ref, "@alice:matrix.org"));
    assertTrue(doc.getText(0, doc.getLength()).contains("Alice: hello"));
  }
}
