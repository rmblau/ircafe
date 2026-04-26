package cafe.woden.ircclient.ui.chat.transcript.flow;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerHistoryInsertSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerRuntimeSupport;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptSpoilerFlowSupport {

  public record Context(
      Map<TargetRef, StyledDocument> docs,
      Consumer<TargetRef> targetEnsureHandler,
      ObjLongConsumer<TargetRef> epochNoteHandler,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptSpoilerRuntimeSupport.Context spoilerRuntimeSupportContext,
      ChatTranscriptSpoilerAppendSupport.Context spoilerAppendSupportContext,
      ChatTranscriptSpoilerHistoryInsertSupport.Context spoilerHistoryInsertSupportContext,
      Consumer<TargetRef> filteredInsertRunEndHandler) {
    public Context {
      Objects.requireNonNull(docs, "docs");
      Objects.requireNonNull(targetEnsureHandler, "targetEnsureHandler");
      Objects.requireNonNull(epochNoteHandler, "epochNoteHandler");
      Objects.requireNonNull(filterRoutingSupport, "filterRoutingSupport");
      Objects.requireNonNull(spoilerRuntimeSupportContext, "spoilerRuntimeSupportContext");
      Objects.requireNonNull(spoilerAppendSupportContext, "spoilerAppendSupportContext");
      Objects.requireNonNull(
          spoilerHistoryInsertSupportContext, "spoilerHistoryInsertSupportContext");
      Objects.requireNonNull(filteredInsertRunEndHandler, "filteredInsertRunEndHandler");
    }
  }

  private ChatTranscriptSpoilerFlowSupport() {}

  public static void appendSpoiler(
      Context context, TargetRef ref, String fromNick, String text, Long tsEpochMs) {
    if (context == null) {
      return;
    }

    context.targetEnsureHandler().accept(ref);
    if (tsEpochMs != null) {
      context.epochNoteHandler().accept(ref, tsEpochMs);
    }
    StyledDocument doc = context.docs().get(ref);
    appendSpoiler(context, doc, ref, fromNick, text, tsEpochMs);
  }

  public static void appendSpoiler(
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

  public static int insertSpoilerFromHistory(
      Context context, TargetRef ref, int insertAt, String fromNick, String text, long tsEpochMs) {
    if (context == null) {
      return Math.max(0, insertAt);
    }

    context.targetEnsureHandler().accept(ref);
    context.epochNoteHandler().accept(ref, tsEpochMs);
    StyledDocument doc = context.docs().get(ref);
    return insertSpoilerFromHistory(context, doc, ref, insertAt, fromNick, text, tsEpochMs);
  }

  public static int insertSpoilerFromHistory(
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
