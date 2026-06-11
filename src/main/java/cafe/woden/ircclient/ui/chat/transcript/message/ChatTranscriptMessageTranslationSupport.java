package cafe.woden.ircclient.ui.chat.transcript.message;

import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeMessageId;

import cafe.woden.ircclient.app.api.MessageTranslation;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import java.util.Objects;
import java.util.function.BiFunction;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptMessageTranslationSupport {

  public static final String AUX_ROW_KIND_TRANSLATION = "translation";

  @FunctionalInterface
  public interface PresenceBlockShiftHandler {
    void shift(TargetRef ref, int insertAt, int delta);
  }

  private final ChatStyles styles;
  private final ChatRichTextRenderer renderer;
  private final BiFunction<AttributeSet, LineMeta, SimpleAttributeSet> withLineMeta;
  private final BiFunction<AttributeSet, String, SimpleAttributeSet> withAuxiliaryRowKind;
  private final BiFunction<StyledDocument, Integer, Integer> ensureAtLineStartForInsert;
  private final PresenceBlockShiftHandler shiftPresenceBlock;

  public ChatTranscriptMessageTranslationSupport(
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      BiFunction<AttributeSet, LineMeta, SimpleAttributeSet> withLineMeta,
      BiFunction<AttributeSet, String, SimpleAttributeSet> withAuxiliaryRowKind,
      BiFunction<StyledDocument, Integer, Integer> ensureAtLineStartForInsert,
      PresenceBlockShiftHandler shiftPresenceBlock) {
    this.styles = Objects.requireNonNull(styles, "styles");
    this.renderer = renderer;
    this.withLineMeta = Objects.requireNonNull(withLineMeta, "withLineMeta");
    this.withAuxiliaryRowKind =
        Objects.requireNonNull(withAuxiliaryRowKind, "withAuxiliaryRowKind");
    this.ensureAtLineStartForInsert =
        Objects.requireNonNull(ensureAtLineStartForInsert, "ensureAtLineStartForInsert");
    this.shiftPresenceBlock = Objects.requireNonNull(shiftPresenceBlock, "shiftPresenceBlock");
  }

  public boolean applyMessageTranslation(
      TargetRef ref, StyledDocument doc, MessageTranslation translation, long translatedAtEpochMs) {
    if (ref == null || doc == null || translation == null) return false;
    String targetMsgId = normalizeMessageId(translation.targetMessageId());
    String translatedText = Objects.toString(translation.translatedText(), "");
    if (targetMsgId.isEmpty() || translatedText.isBlank()) return false;

    ChatTranscriptDocumentSupport.MessageLine messageLine =
        ChatTranscriptDocumentSupport.findMessageLine(doc, targetMsgId);
    if (messageLine == null) return false;

    removeTranslationRow(ref, doc, targetMsgId);
    messageLine = ChatTranscriptDocumentSupport.findMessageLine(doc, targetMsgId);
    if (messageLine == null) return false;

    int beforeLen = doc.getLength();
    int pos = ChatTranscriptDocumentSupport.nextLineStart(doc, messageLine.lineStart());
    pos = ensureAtLineStartForInsert.apply(doc, pos);
    int insertionStart = pos;

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref,
            LogKind.STATUS,
            LogDirection.SYSTEM,
            null,
            translatedAtEpochMs,
            null,
            targetMsgId,
            java.util.Map.of());
    SimpleAttributeSet attrs =
        withAuxiliaryRowKind.apply(
            withLineMeta.apply(styles.status(), meta), AUX_ROW_KIND_TRANSLATION);
    attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
    String rendered = renderTranslationText(translation);

    try {
      if (renderer != null) {
        pos = renderer.insertRichTextAt(doc, ref, rendered, attrs, pos);
      } else {
        pos = ChatRichTextRenderer.insertStyledTextAt(doc, rendered, attrs, pos);
      }
      doc.insertString(
          pos,
          "\n",
          withAuxiliaryRowKind.apply(
              withLineMeta.apply(styles.timestamp(), meta), AUX_ROW_KIND_TRANSLATION));
    } catch (Exception ignored) {
      return false;
    }

    int delta = doc.getLength() - beforeLen;
    shiftPresenceBlock.shift(ref, insertionStart, delta);
    return true;
  }

  public void clearTranslationForMessage(
      TargetRef ref, StyledDocument doc, String targetMessageId) {
    if (ref == null || doc == null) return;
    removeTranslationRow(ref, doc, targetMessageId);
  }

  private void removeTranslationRow(TargetRef ref, StyledDocument doc, String targetMessageId) {
    String targetMsgId = normalizeMessageId(targetMessageId);
    if (targetMsgId.isEmpty()) return;
    int start =
        ChatTranscriptDocumentSupport.findAuxiliaryLineStartByMessageId(
            doc, targetMsgId, AUX_ROW_KIND_TRANSLATION);
    if (start < 0) return;
    int lineEnd = ChatTranscriptDocumentSupport.lineEndOffsetForLineStart(doc, start);
    int removeLen = Math.max(0, lineEnd - start);
    if (removeLen <= 0) return;
    try {
      doc.remove(start, removeLen);
      shiftPresenceBlock.shift(ref, start, -removeLen);
    } catch (Exception ignored) {
    }
  }

  private static String renderTranslationText(MessageTranslation translation) {
    String languageLabel = renderLanguageLabel(translation);
    String text = Objects.toString(translation.translatedText(), "");
    if (languageLabel.isBlank()) {
      return "[translated] " + text;
    }
    return "[" + languageLabel + "] " + text;
  }

  private static String renderLanguageLabel(MessageTranslation translation) {
    String source = Objects.toString(translation.sourceLanguage(), "").trim();
    String target = Objects.toString(translation.targetLanguage(), "").trim();
    String provider = Objects.toString(translation.provider(), "").trim();
    StringBuilder label = new StringBuilder();
    if (!source.isBlank() && !target.isBlank()) {
      label.append(source).append(" -> ").append(target);
    } else if (!target.isBlank()) {
      label.append(target);
    } else if (!source.isBlank()) {
      label.append(source);
    }
    if (!provider.isBlank()) {
      if (!label.isEmpty()) {
        label.append(" via ");
      }
      label.append(provider);
    }
    return label.toString();
  }
}
