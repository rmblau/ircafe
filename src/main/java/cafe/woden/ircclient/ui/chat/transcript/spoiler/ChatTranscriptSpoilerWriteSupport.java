package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

final class ChatTranscriptSpoilerWriteSupport {

  @FunctionalInterface
  interface RevealHandlerFactory {
    BooleanSupplier create(Position spoilerPos, SpoilerMessageComponent component);
  }

  record Context(
      ChatStyles styles,
      ChatTranscriptSpoilerComponentSupport.Context componentContext,
      BiFunction<AttributeSet, FilterEngine.Match, SimpleAttributeSet> filterMatchApplier) {}

  private ChatTranscriptSpoilerWriteSupport() {}

  static ChatTranscriptSpoilerLineSupport.WriteResult writeLineAt(
      Context context,
      StyledDocument doc,
      TargetRef ref,
      int offset,
      String fromNick,
      String tsPrefix,
      LineMeta meta,
      FilterEngine.Match match,
      RevealHandlerFactory revealHandlerFactory)
      throws Exception {
    SpoilerMessageComponent component =
        ChatTranscriptSpoilerComponentSupport.create(
            context.componentContext(), ref, fromNick, tsPrefix);
    SimpleAttributeSet componentAttrs =
        ChatTranscriptLineMetaSupport.bind(context.styles().message(), meta);
    SimpleAttributeSet timestampAttrs =
        ChatTranscriptLineMetaSupport.bind(context.styles().timestamp(), meta);

    if (match != null && context.filterMatchApplier() != null) {
      componentAttrs = context.filterMatchApplier().apply(componentAttrs, match);
      timestampAttrs = context.filterMatchApplier().apply(timestampAttrs, match);
    }

    return ChatTranscriptSpoilerLineSupport.writeLineAt(
        doc,
        offset,
        component,
        componentAttrs,
        timestampAttrs,
        spoilerPos ->
            revealHandlerFactory != null
                ? revealHandlerFactory.create(spoilerPos, component)
                : () -> false);
  }
}
