package cafe.woden.ircclient.ui.chat.transcript.spoiler;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import java.util.Objects;
import java.util.function.BiFunction;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptSpoilerRevealSupport {

  public record Context(
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      NickColorService nickColors,
      BiFunction<TargetRef, String, String> renderTranscriptFrom) {}

  private ChatTranscriptSpoilerRevealSupport() {}

  static boolean revealInPlace(
      Context context,
      StyledDocument doc,
      TargetRef ref,
      Position anchor,
      SpoilerMessageComponent expected,
      String tsPrefix,
      String fromNick,
      String messageText) {
    if (context == null || context.styles() == null || doc == null || anchor == null) return false;
    try {
      int len = doc.getLength();
      if (len <= 0) return false;

      int guess = anchor.getOffset();
      if (guess < 0) guess = 0;
      if (guess >= len) guess = len - 1;

      int off = findSpoilerOffset(doc, guess, expected);
      if (off < 0) return false;
      Element el = doc.getCharacterElement(off);
      if (el == null) return false;
      AttributeSet attrs = el.getAttributes();
      Object component = attrs != null ? StyleConstants.getComponent(attrs) : null;
      if (!(component instanceof SpoilerMessageComponent)) return false;
      if (expected != null && component != expected) return false;

      AttributeSet tsStyle =
          ChatTranscriptLineMetaSupport.withExistingMeta(context.styles().timestamp(), attrs);
      AttributeSet msgStyle =
          ChatTranscriptLineMetaSupport.withExistingMeta(context.styles().message(), attrs);
      int removeLen = removalLength(doc, off);
      doc.remove(off, removeLen);

      int pos = off;
      String tsText = Objects.toString(tsPrefix, "");
      if (!tsText.isBlank()) {
        doc.insertString(pos, tsText, tsStyle);
        pos += tsText.length();
      }
      if (fromNick != null && !fromNick.isBlank()) {
        AttributeSet fromStyle = context.styles().from();
        NickColorService nickColors = context.nickColors();
        if (nickColors != null && nickColors.enabled()) {
          fromStyle = nickColors.forNick(fromNick, fromStyle);
        }
        fromStyle = ChatTranscriptLineMetaSupport.withExistingMeta(fromStyle, attrs);
        String renderedFrom = renderedFrom(context, ref, fromNick);
        String prefix = renderedFrom + ": ";
        doc.insertString(pos, prefix, fromStyle);
        pos += prefix.length();
      }

      DefaultStyledDocument inner = new DefaultStyledDocument();
      insertRenderedMessage(context, inner, ref, Objects.toString(messageText, ""), msgStyle);
      pos = insertStyled(inner, doc, pos);
      doc.insertString(pos, "\n", tsStyle);
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  private static String renderedFrom(Context context, TargetRef ref, String fromNick) {
    if (context.renderTranscriptFrom() == null) {
      return Objects.toString(fromNick, "");
    }
    return Objects.toString(context.renderTranscriptFrom().apply(ref, fromNick), "");
  }

  private static void insertRenderedMessage(
      Context context, StyledDocument doc, TargetRef ref, String msg, AttributeSet msgStyle) {
    try {
      if (context.renderer() != null) {
        context.renderer().insertRichText(doc, ref, msg, new SimpleAttributeSet(msgStyle));
      } else {
        ChatRichTextRenderer.insertStyledTextAt(doc, msg, msgStyle, 0);
      }
    } catch (Exception ignored) {
      try {
        doc.remove(0, doc.getLength());
        ChatRichTextRenderer.insertStyledTextAt(doc, msg, msgStyle, 0);
      } catch (Exception ignored2) {
      }
    }
  }

  private static int removalLength(StyledDocument doc, int offset) {
    int removeLen = 1;
    if (doc == null || offset < 0) return removeLen;
    if (offset + 1 < doc.getLength()) {
      try {
        String next = doc.getText(offset + 1, 1);
        if ("\n".equals(next)) removeLen = 2;
      } catch (Exception ignored) {
      }
    }
    return removeLen;
  }

  private static int findSpoilerOffset(
      StyledDocument doc, int guess, SpoilerMessageComponent expected) {
    if (doc == null) return -1;
    int len = doc.getLength();
    if (len <= 0) return -1;

    int start = Math.max(0, guess - 256);
    int end = Math.min(len - 1, guess + 256);
    for (int i = start; i <= end; i++) {
      try {
        Element el = doc.getCharacterElement(i);
        if (el == null) continue;
        AttributeSet attrs = el.getAttributes();
        Object component = attrs != null ? StyleConstants.getComponent(attrs) : null;
        if (component instanceof SpoilerMessageComponent) {
          if (expected == null || component == expected) return i;
        }
      } catch (Exception ignored) {
      }
    }
    return -1;
  }

  private static int insertStyled(StyledDocument src, StyledDocument dest, int pos) {
    if (src == null || dest == null) return pos;
    try {
      int len = src.getLength();
      int i = 0;
      while (i < len) {
        Element el = src.getCharacterElement(i);
        if (el == null) break;

        int start = Math.max(0, Math.min(el.getStartOffset(), len));
        int end = Math.max(start, Math.min(el.getEndOffset(), len));
        if (end <= start) {
          i = Math.min(len, i + 1);
          continue;
        }

        String text = src.getText(start, end - start);
        if (text != null && !text.isEmpty()) {
          dest.insertString(pos, text, el.getAttributes());
          pos += text.length();
        }
        i = end;
      }
    } catch (Exception ignored) {
    }
    return pos;
  }
}
