package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

final class ChatTranscriptActionAppendSupport {

  @FunctionalInterface
  interface RenderedFromResolver {
    String render(TargetRef ref, String from);
  }

  @FunctionalInterface
  interface FilterMatchStyler {
    SimpleAttributeSet apply(AttributeSet base, FilterEngine.Match match);
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
      ChatTranscriptManualPreviewSupport manualPreviewSupport,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      RenderedFromResolver renderedFromResolver,
      FilterMatchStyler filterMatchStyler,
      Consumer<StyledDocument> ensureAtLineStart,
      TranscriptLineCapEnforcer transcriptLineCapEnforcer,
      PendingReadMarkerRenderer pendingReadMarkerRenderer) {
    Context {
      Objects.requireNonNull(styles, "styles");
      Objects.requireNonNull(senderStyleSupportContext, "senderStyleSupportContext");
      Objects.requireNonNull(renderer, "renderer");
      Objects.requireNonNull(manualPreviewSupport, "manualPreviewSupport");
      Objects.requireNonNull(messageCatalogSupport, "messageCatalogSupport");
      Objects.requireNonNull(renderedFromResolver, "renderedFromResolver");
      Objects.requireNonNull(filterMatchStyler, "filterMatchStyler");
      Objects.requireNonNull(ensureAtLineStart, "ensureAtLineStart");
      Objects.requireNonNull(transcriptLineCapEnforcer, "transcriptLineCapEnforcer");
      Objects.requireNonNull(pendingReadMarkerRenderer, "pendingReadMarkerRenderer");
    }
  }

  private ChatTranscriptActionAppendSupport() {}

  static void appendVisibleAction(
      Context context,
      TargetRef ref,
      StyledDocument doc,
      ChatTranscriptMessageCatalogSupport.State messageCatalogState,
      String from,
      String action,
      boolean outgoingLocalEcho,
      boolean allowEmbeds,
      long tsEpochMs,
      String notificationRuleHighlightColor,
      Map<String, String> ircv3Tags,
      LineMeta meta,
      FilterEngine.Match match,
      boolean timestampsIncludeChatMessages,
      boolean imageEmbedsEnabled,
      boolean linkPreviewsEnabled) {
    if (context == null || ref == null || doc == null || meta == null) {
      return;
    }

    String body = action == null ? "" : action;
    context.ensureAtLineStart().accept(doc);

    try {
      SimpleAttributeSet tsStyle =
          ChatTranscriptLineMetaSupport.bind(context.styles().timestamp(), meta);
      if (match != null) {
        tsStyle = context.filterMatchStyler().apply(tsStyle, match);
      }

      ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
          ChatTranscriptSenderStyleSupport.prepareAction(
              context.senderStyleSupportContext(),
              meta,
              from,
              outgoingLocalEcho,
              notificationRuleHighlightColor);
      SimpleAttributeSet fromStyle = preparedStyles.fromStyle();
      SimpleAttributeSet messageStyle = preparedStyles.messageStyle();
      if (match != null) {
        fromStyle = context.filterMatchStyler().apply(fromStyle, match);
        messageStyle = context.filterMatchStyler().apply(messageStyle, match);
      }

      String renderedFrom = context.renderedFromResolver().render(ref, from);
      ChatTranscriptActionLineSupport.WriteResult writeResult =
          ChatTranscriptActionLineSupport.writeLineAt(
              doc,
              ref,
              doc.getLength(),
              body,
              renderedFrom,
              tsStyle,
              fromStyle,
              messageStyle,
              tsEpochMs,
              context.timestamps(),
              context.renderer(),
              timestampsIncludeChatMessages,
              false);
      int lineEndOffset = writeResult.lineEndOffset();
      context
          .messageCatalogSupport()
          .recordInsertedMessage(messageCatalogState, meta, renderedFrom, body);

      if (allowEmbeds) {
        context
            .manualPreviewSupport()
            .appendBlockedPreviewMarkersForAppend(
                ref,
                doc,
                lineEndOffset,
                body,
                from,
                ircv3Tags,
                meta,
                match,
                imageEmbedsEnabled,
                linkPreviewsEnabled,
                context.filterMatchStyler()::apply);
      }
      context.transcriptLineCapEnforcer().enforce(ref, doc);
      context.pendingReadMarkerRenderer().render(ref, tsEpochMs);
    } catch (Exception ignored) {
    }
  }
}
