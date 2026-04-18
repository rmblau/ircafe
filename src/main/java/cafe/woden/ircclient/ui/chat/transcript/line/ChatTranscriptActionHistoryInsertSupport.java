package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Objects;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

final class ChatTranscriptActionHistoryInsertSupport {

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

  @FunctionalInterface
  interface PendingReadMarkerRenderer {
    void render(TargetRef ref, Long lineEpochMs);
  }

  record Context(
      ChatStyles styles,
      ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext,
      ChatTimestampFormatter timestamps,
      ChatRichTextRenderer renderer,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      RenderedFromResolver renderedFromResolver,
      FilterMatchStyler filterMatchStyler,
      InsertAtNormalizer insertAtNormalizer,
      InsertLineStartEnsurer insertLineStartEnsurer,
      PresenceBlockShifter presenceBlockShifter,
      TranscriptLineCapEnforcer transcriptLineCapEnforcer,
      PendingReadMarkerRenderer pendingReadMarkerRenderer) {
    Context {
      Objects.requireNonNull(styles, "styles");
      Objects.requireNonNull(senderStyleSupportContext, "senderStyleSupportContext");
      Objects.requireNonNull(renderer, "renderer");
      Objects.requireNonNull(messageCatalogSupport, "messageCatalogSupport");
      Objects.requireNonNull(renderedFromResolver, "renderedFromResolver");
      Objects.requireNonNull(filterMatchStyler, "filterMatchStyler");
      Objects.requireNonNull(insertAtNormalizer, "insertAtNormalizer");
      Objects.requireNonNull(insertLineStartEnsurer, "insertLineStartEnsurer");
      Objects.requireNonNull(presenceBlockShifter, "presenceBlockShifter");
      Objects.requireNonNull(transcriptLineCapEnforcer, "transcriptLineCapEnforcer");
      Objects.requireNonNull(pendingReadMarkerRenderer, "pendingReadMarkerRenderer");
    }
  }

  private ChatTranscriptActionHistoryInsertSupport() {}

  static int insertVisibleAction(
      Context context,
      TargetRef ref,
      StyledDocument doc,
      ChatTranscriptMessageCatalogSupport.State messageCatalogState,
      int insertAt,
      String from,
      String action,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      LineMeta meta,
      FilterEngine.Match match,
      boolean timestampsIncludeChatMessages,
      boolean deferRichText) {
    return insertVisibleAction(
        context,
        ref,
        doc,
        messageCatalogState,
        insertAt,
        from,
        action,
        outgoingLocalEcho,
        tsEpochMs,
        meta,
        match,
        timestampsIncludeChatMessages,
        deferRichText,
        true,
        true);
  }

  static int insertVisibleAction(
      Context context,
      TargetRef ref,
      StyledDocument doc,
      ChatTranscriptMessageCatalogSupport.State messageCatalogState,
      int insertAt,
      String from,
      String action,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      LineMeta meta,
      FilterEngine.Match match,
      boolean timestampsIncludeChatMessages,
      boolean deferRichText,
      boolean enforceLineCap,
      boolean renderPendingReadMarker) {
    if (context == null || ref == null || doc == null) {
      return Math.max(0, insertAt);
    }

    int beforeLen = doc.getLength();
    int pos = context.insertAtNormalizer().normalize(doc, insertAt);
    pos = context.insertLineStartEnsurer().ensure(doc, pos);
    int insertionStart = pos;
    String body = action == null ? "" : action;

    try {
      SimpleAttributeSet timestampStyle =
          ChatTranscriptLineMetaSupport.bind(context.styles().timestamp(), meta);
      if (match != null) {
        timestampStyle = context.filterMatchStyler().apply(timestampStyle, match);
      }

      ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
          ChatTranscriptSenderStyleSupport.prepareAction(
              context.senderStyleSupportContext(), meta, from, outgoingLocalEcho, null);
      SimpleAttributeSet fromStyle = preparedStyles.fromStyle();
      SimpleAttributeSet messageStyle = preparedStyles.messageStyle();
      if (match != null) {
        fromStyle = context.filterMatchStyler().apply(fromStyle, match);
        messageStyle = context.filterMatchStyler().apply(messageStyle, match);
      }

      String renderedFrom = context.renderedFromResolver().render(ref, from);
      pos =
          ChatTranscriptActionLineSupport.writeLineAt(
                  doc,
                  ref,
                  pos,
                  body,
                  renderedFrom,
                  timestampStyle,
                  fromStyle,
                  messageStyle,
                  tsEpochMs,
                  context.timestamps(),
                  context.renderer(),
                  timestampsIncludeChatMessages,
                  deferRichText)
              .nextOffset();
      context
          .messageCatalogSupport()
          .recordInsertedMessage(messageCatalogState, meta, renderedFrom, body);
    } catch (Exception ignored) {
    }

    int delta = doc.getLength() - beforeLen;
    context.presenceBlockShifter().shift(ref, insertionStart, delta);
    if (enforceLineCap) {
      int trimmed = context.transcriptLineCapEnforcer().enforce(ref, doc);
      if (trimmed > 0) {
        pos = Math.max(0, pos - trimmed);
      }
    }
    if (renderPendingReadMarker) {
      context.pendingReadMarkerRenderer().render(ref, tsEpochMs);
    }
    return pos;
  }
}
