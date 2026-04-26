package cafe.woden.ircclient.ui.chat.transcript.line;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.awt.Component;
import java.util.Map;
import java.util.Objects;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptTextAppendSupport {

  @FunctionalInterface
  public interface RenderedFromResolver {
    String render(TargetRef ref, String from);
  }

  @FunctionalInterface
  public interface FilterMatchStyler {
    SimpleAttributeSet apply(AttributeSet base, FilterEngine.Match match);
  }

  @FunctionalInterface
  public interface TranscriptLineCapEnforcer {
    int enforce(TargetRef ref, StyledDocument doc);
  }

  @FunctionalInterface
  public interface PendingReadMarkerRenderer {
    void render(TargetRef ref, Long lineEpochMs);
  }

  public record Context(
      ChatStyles styles,
      ChatTimestampFormatter timestamps,
      ChatRichTextRenderer renderer,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      ChatTranscriptManualPreviewSupport manualPreviewSupport,
      RenderedFromResolver renderedFromResolver,
      FilterMatchStyler filterMatchStyler,
      TranscriptLineCapEnforcer transcriptLineCapEnforcer,
      PendingReadMarkerRenderer pendingReadMarkerRenderer) {
    public Context {
      Objects.requireNonNull(styles, "styles");
      Objects.requireNonNull(messageCatalogSupport, "messageCatalogSupport");
      Objects.requireNonNull(manualPreviewSupport, "manualPreviewSupport");
      Objects.requireNonNull(renderedFromResolver, "renderedFromResolver");
      Objects.requireNonNull(filterMatchStyler, "filterMatchStyler");
      Objects.requireNonNull(transcriptLineCapEnforcer, "transcriptLineCapEnforcer");
      Objects.requireNonNull(pendingReadMarkerRenderer, "pendingReadMarkerRenderer");
    }
  }

  private ChatTranscriptTextAppendSupport() {}

  public static void appendVisibleLine(
      Context context,
      TargetRef ref,
      StyledDocument doc,
      ChatTranscriptMessageCatalogSupport.State messageCatalogState,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet messageStyle,
      boolean allowEmbeds,
      LineMeta meta,
      FilterEngine.Match match,
      Component tailComponent,
      AttributeSet tailAttrs,
      boolean timestampsIncludeChatMessages,
      boolean timestampsIncludePresenceMessages,
      boolean deferRichText,
      boolean imageEmbedsEnabled,
      boolean linkPreviewsEnabled) {
    if (context == null || ref == null || doc == null) {
      return;
    }

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
      AttributeSet preparedTailStyle = null;
      if (tailComponent != null) {
        SimpleAttributeSet attrs =
            new SimpleAttributeSet(tailAttrs != null ? tailAttrs : preparedMessageStyle);
        attrs = ChatTranscriptLineMetaSupport.bind(attrs, meta);
        if (match != null) {
          attrs = context.filterMatchStyler().apply(attrs, match);
        }
        preparedTailStyle = attrs;
      }

      ChatTranscriptTextLineSupport.WriteResult writeResult =
          ChatTranscriptTextLineSupport.writeLineAt(
              doc,
              ref,
              doc.getLength(),
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
              tailComponent,
              preparedTailStyle);
      int lineEndOffset = writeResult.lineEndOffset();
      context
          .messageCatalogSupport()
          .recordInsertedMessage(messageCatalogState, meta, renderedFrom, text);

      if (allowEmbeds) {
        String embedFrom = meta != null ? meta.fromNick() : from;
        Map<String, String> embedTags = meta != null ? meta.ircv3TagsMap() : Map.of();
        context
            .manualPreviewSupport()
            .appendBlockedPreviewMarkersForAppend(
                ref,
                doc,
                lineEndOffset,
                text,
                embedFrom,
                embedTags,
                meta,
                match,
                imageEmbedsEnabled,
                linkPreviewsEnabled,
                context.filterMatchStyler()::apply);
      }
      context.transcriptLineCapEnforcer().enforce(ref, doc);
      context.pendingReadMarkerRenderer().render(ref, epochMs);
    } catch (Exception ignored) {
    }
  }
}
