package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import java.util.function.Function;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

final class ChatTranscriptSpoilerLineSupport {

  @FunctionalInterface
  interface RevealHandlerFactory extends Function<Position, java.util.function.BooleanSupplier> {}

  record WriteResult(int lineEndOffset, int nextOffset) {}

  private ChatTranscriptSpoilerLineSupport() {}

  static WriteResult writeLineAt(
      StyledDocument doc,
      int offset,
      SpoilerMessageComponent component,
      AttributeSet componentStyle,
      AttributeSet timestampStyle,
      RevealHandlerFactory revealHandlerFactory)
      throws Exception {
    SimpleAttributeSet componentAttrs = new SimpleAttributeSet(componentStyle);
    StyleConstants.setComponent(componentAttrs, component);

    int pos = Math.max(0, offset);
    doc.insertString(pos, " ", componentAttrs);
    Position spoilerPos = doc.createPosition(pos);
    if (component != null && revealHandlerFactory != null) {
      component.setOnReveal(revealHandlerFactory.apply(spoilerPos));
    }

    int lineEndOffset = pos + 1;
    doc.insertString(lineEndOffset, "\n", timestampStyle);
    return new WriteResult(lineEndOffset, lineEndOffset + 1);
  }
}
