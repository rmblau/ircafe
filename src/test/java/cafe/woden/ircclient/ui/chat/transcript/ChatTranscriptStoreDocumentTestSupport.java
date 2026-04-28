package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.ui.chat.fold.MessageReactionsComponent;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDeliveryIndicatorSupport;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/** Document-level assertions and helpers for transcript store tests. */
final class ChatTranscriptStoreDocumentTestSupport {

  private ChatTranscriptStoreDocumentTestSupport() {}

  static String transcriptText(StyledDocument doc) throws Exception {
    return doc.getText(0, doc.getLength());
  }

  static MessageReactionsComponent reactionComponent(StyledDocument doc) {
    Element root = doc.getDefaultRootElement();
    if (root == null) return null;
    int len = doc.getLength();
    for (int i = 0; i < root.getElementCount(); i++) {
      Element line = root.getElement(i);
      if (line == null) continue;
      int start = Math.max(0, line.getStartOffset());
      if (start >= len) continue;
      Object comp = StyleConstants.getComponent(doc.getCharacterElement(start).getAttributes());
      if (comp instanceof MessageReactionsComponent reactions) {
        return reactions;
      }
    }
    return null;
  }

  static int lineCount(StyledDocument doc) {
    try {
      String text = transcriptText(doc);
      return (int) text.chars().filter(ch -> ch == '\n').count();
    } catch (Exception ignored) {
      return 0;
    }
  }

  static String transcriptTextUnchecked(StyledDocument doc) {
    try {
      return transcriptText(doc);
    } catch (Exception ignored) {
      return "";
    }
  }

  static int inlineComponentCount(StyledDocument doc, Class<?> componentType) {
    return ChatTranscriptDeliveryIndicatorSupport.inlineComponentCount(doc, componentType);
  }
}
