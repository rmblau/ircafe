package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Objects;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

final class ChatTranscriptTextInsertSupport {

  @FunctionalInterface
  interface RenderedFromResolver {
    String render(TargetRef ref, String from);
  }

  @FunctionalInterface
  interface FilterMatchStyler {
    SimpleAttributeSet apply(AttributeSet base, FilterEngine.Match match);
  }

  @FunctionalInterface
  interface InsertAtNormalizer {
    int normalize(StyledDocument doc, int insertAt);
  }

  @FunctionalInterface
  interface InsertLineStartEnsurer {
    int ensure(StyledDocument doc, int insertAt);
  }

  @FunctionalInterface
  interface PresenceBlockShifter {
    void shift(TargetRef ref, int insertAt, int delta);
  }

  @FunctionalInterface
  interface TranscriptLineCapEnforcer {
    int enforce(TargetRef ref, StyledDocument doc);
  }

  record Context(
      ChatStyles styles,
      ChatTimestampFormatter timestamps,
      ChatRichTextRenderer renderer,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      RenderedFromResolver renderedFromResolver,
      FilterMatchStyler filterMatchStyler,
      InsertAtNormalizer insertAtNormalizer,
      InsertLineStartEnsurer insertLineStartEnsurer,
      PresenceBlockShifter presenceBlockShifter,
      TranscriptLineCapEnforcer transcriptLineCapEnforcer) {
    Context {
      Objects.requireNonNull(styles, "styles");
      Objects.requireNonNull(renderer, "renderer");
      Objects.requireNonNull(messageCatalogSupport, "messageCatalogSupport");
      Objects.requireNonNull(renderedFromResolver, "renderedFromResolver");
      Objects.requireNonNull(filterMatchStyler, "filterMatchStyler");
      Objects.requireNonNull(insertAtNormalizer, "insertAtNormalizer");
      Objects.requireNonNull(insertLineStartEnsurer, "insertLineStartEnsurer");
      Objects.requireNonNull(presenceBlockShifter, "presenceBlockShifter");
      Objects.requireNonNull(transcriptLineCapEnforcer, "transcriptLineCapEnforcer");
    }
  }

  private ChatTranscriptTextInsertSupport() {}

  static int insertVisibleLine(
      Context context,
      TargetRef ref,
      StyledDocument doc,
      ChatTranscriptMessageCatalogSupport.State messageCatalogState,
      int insertAt,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet messageStyle,
      LineMeta meta,
      FilterEngine.Match match,
      boolean timestampsIncludeChatMessages,
      boolean timestampsIncludePresenceMessages,
      boolean deferRichText) {
    if (context == null || ref == null || doc == null) {
      return Math.max(0, insertAt);
    }

    int beforeLen = doc.getLength();
    int pos = context.insertAtNormalizer().normalize(doc, insertAt);
    pos = context.insertLineStartEnsurer().ensure(doc, pos);
    int insertionStart = pos;

    Long epochMs = meta != null ? meta.epochMs() : null;
    String renderedFrom = context.renderedFromResolver().render(ref, from);
    SimpleAttributeSet timestampStyle =
        ChatTranscriptLineMetaSupport.bind(context.styles().timestamp(), meta);
    SimpleAttributeSet preparedFromStyle =
        ChatTranscriptLineMetaSupport.bind(
            fromStyle != null ? fromStyle : context.styles().from(), meta);
    SimpleAttributeSet preparedMessageStyle =
        ChatTranscriptLineMetaSupport.bind(
            messageStyle != null ? messageStyle : context.styles().message(), meta);

    if (match != null) {
      timestampStyle = context.filterMatchStyler().apply(timestampStyle, match);
      preparedFromStyle = context.filterMatchStyler().apply(preparedFromStyle, match);
      preparedMessageStyle = context.filterMatchStyler().apply(preparedMessageStyle, match);
    }

    try {
      pos =
          ChatTranscriptTextLineSupport.writeLineAt(
                  doc,
                  ref,
                  pos,
                  text,
                  renderedFrom,
                  timestampStyle,
                  preparedFromStyle,
                  preparedMessageStyle,
                  epochMs,
                  context.timestamps(),
                  context.renderer(),
                  timestampsIncludeChatMessages,
                  timestampsIncludePresenceMessages,
                  deferRichText,
                  null,
                  null)
              .nextOffset();
      context
          .messageCatalogSupport()
          .recordInsertedMessage(messageCatalogState, meta, renderedFrom, text);
    } catch (Exception ignored) {
    }

    int delta = doc.getLength() - beforeLen;
    context.presenceBlockShifter().shift(ref, insertionStart, delta);
    int trimmed = context.transcriptLineCapEnforcer().enforce(ref, doc);
    if (trimmed > 0) {
      pos = Math.max(0, pos - trimmed);
    }
    return pos;
  }
}
