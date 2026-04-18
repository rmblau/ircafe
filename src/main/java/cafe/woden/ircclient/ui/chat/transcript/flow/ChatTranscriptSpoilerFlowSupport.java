package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;

final class ChatTranscriptSpoilerFlowSupport {

  record Context(
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptSpoilerRuntimeSupport.Context spoilerRuntimeSupportContext,
      ChatTranscriptSpoilerAppendSupport.Context spoilerAppendSupportContext,
      ChatTranscriptSpoilerHistoryInsertSupport.Context spoilerHistoryInsertSupportContext,
      Consumer<TargetRef> filteredInsertRunEndHandler) {
    Context {
      Objects.requireNonNull(filterRoutingSupport, "filterRoutingSupport");
      Objects.requireNonNull(spoilerRuntimeSupportContext, "spoilerRuntimeSupportContext");
      Objects.requireNonNull(spoilerAppendSupportContext, "spoilerAppendSupportContext");
      Objects.requireNonNull(
          spoilerHistoryInsertSupportContext, "spoilerHistoryInsertSupportContext");
      Objects.requireNonNull(filteredInsertRunEndHandler, "filteredInsertRunEndHandler");
    }
  }

  private ChatTranscriptSpoilerFlowSupport() {}

  static void appendSpoiler(
      Context context,
      StyledDocument doc,
      TargetRef ref,
      String fromNick,
      String text,
      Long tsEpochMs) {
    if (context == null || doc == null || ref == null) {
      return;
    }

    long effectiveEpochMs = tsEpochMs != null ? tsEpochMs : System.currentTimeMillis();
    ChatTranscriptFilterRoutingSupport.VisibleAppend prepared =
        context
            .filterRoutingSupport()
            .prepareVisibleTextAppendWithMatch(
                ref,
                LogKind.SPOILER,
                LogDirection.IN,
                fromNick,
                text,
                effectiveEpochMs,
                "",
                Map.of());
    if (prepared == null) {
      return;
    }

    String msg = Objects.toString(text, "");
    String tsPrefix =
        ChatTranscriptSpoilerRuntimeSupport.timestampPrefix(
            context.spoilerRuntimeSupportContext(), tsEpochMs);
    ChatTranscriptSpoilerAppendSupport.appendVisibleSpoiler(
        context.spoilerAppendSupportContext(),
        doc,
        ref,
        fromNick,
        tsPrefix,
        prepared.meta(),
        prepared.match(),
        (spoilerPos, component) ->
            () ->
                ChatTranscriptSpoilerRuntimeSupport.revealInPlace(
                    context.spoilerRuntimeSupportContext(),
                    doc,
                    ref,
                    spoilerPos,
                    component,
                    tsPrefix,
                    fromNick,
                    msg));
  }

  static int insertSpoilerFromHistory(
      Context context,
      StyledDocument doc,
      TargetRef ref,
      int insertAt,
      String fromNick,
      String text,
      long tsEpochMs) {
    if (context == null || doc == null || ref == null) {
      return Math.max(0, insertAt);
    }

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.SPOILER, LogDirection.IN, fromNick, tsEpochMs, null);
    var match =
        context
            .filterRoutingSupport()
            .firstMatch(ref, LogKind.SPOILER, LogDirection.IN, fromNick, text, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        context
            .filterRoutingSupport()
            .handleHiddenTextHistoryInsert(ref, insertAt, fromNick, text, meta, match);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    context.filteredInsertRunEndHandler().accept(ref);

    String msg = Objects.toString(text, "");
    String tsPrefix =
        ChatTranscriptSpoilerRuntimeSupport.timestampPrefix(
            context.spoilerRuntimeSupportContext(), tsEpochMs);
    return ChatTranscriptSpoilerHistoryInsertSupport.insertVisibleSpoiler(
        context.spoilerHistoryInsertSupportContext(),
        doc,
        ref,
        insertAt,
        fromNick,
        tsPrefix,
        meta,
        match,
        (spoilerPos, component) ->
            () ->
                ChatTranscriptSpoilerRuntimeSupport.revealInPlace(
                    context.spoilerRuntimeSupportContext(),
                    doc,
                    ref,
                    spoilerPos,
                    component,
                    tsPrefix,
                    fromNick,
                    msg));
  }
}
