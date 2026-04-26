package cafe.woden.ircclient.ui.chat.transcript.flow;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptTextAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptTextInsertSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.awt.Component;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

/** Shared append/insert line orchestration for visible transcript text rows. */
public final class ChatTranscriptLineFlowSupport {

  @FunctionalInterface
  public interface EpochNoteHandler {
    void note(TargetRef ref, Long epochMs);
  }

  @FunctionalInterface
  public interface PendingHistoryDividerFlusher {
    void flush(TargetRef ref, StyledDocument doc);
  }

  public record Context(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      Consumer<TargetRef> ensureTargetExists,
      EpochNoteHandler noteEpochMs,
      PendingHistoryDividerFlusher flushPendingHistoryDivider,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      Consumer<TargetRef> endFilteredInsertRun,
      Predicate<TargetRef> deferRichTextDuringHistoryBatch,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptTextAppendSupport.Context textAppendSupportContext,
      ChatTranscriptTextInsertSupport.Context textInsertSupportContext,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      BooleanSupplier imageEmbedsEnabled,
      BooleanSupplier linkPreviewsEnabled) {
    public Context {
      Objects.requireNonNull(docs, "docs");
      Objects.requireNonNull(stateByTarget, "stateByTarget");
      Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
      Objects.requireNonNull(noteEpochMs, "noteEpochMs");
      Objects.requireNonNull(flushPendingHistoryDivider, "flushPendingHistoryDivider");
      Objects.requireNonNull(filterRoutingSupport, "filterRoutingSupport");
      Objects.requireNonNull(endFilteredInsertRun, "endFilteredInsertRun");
      Objects.requireNonNull(deferRichTextDuringHistoryBatch, "deferRichTextDuringHistoryBatch");
      Objects.requireNonNull(documentLineSupport, "documentLineSupport");
      Objects.requireNonNull(textAppendSupportContext, "textAppendSupportContext");
      Objects.requireNonNull(textInsertSupportContext, "textInsertSupportContext");
      Objects.requireNonNull(runtimeSettingsSupport, "runtimeSettingsSupport");
      Objects.requireNonNull(imageEmbedsEnabled, "imageEmbedsEnabled");
      Objects.requireNonNull(linkPreviewsEnabled, "linkPreviewsEnabled");
    }

    StyledDocument document(TargetRef ref) {
      return docs.get(ref);
    }

    ChatTranscriptState state(TargetRef ref) {
      return stateByTarget.get(ref);
    }

    boolean shouldDeferRichTextDuringHistoryBatch(TargetRef ref) {
      return deferRichTextDuringHistoryBatch.test(ref);
    }
  }

  public void appendLine(
      Context context,
      TargetRef ref,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      boolean allowEmbeds,
      LineMeta meta,
      Component tailComponent,
      AttributeSet tailAttrs) {
    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.document(ref);

    if (allowEmbeds) {
      context.flushPendingHistoryDivider().flush(ref, doc);
    }

    context.noteEpochMs().note(ref, meta != null ? meta.epochMs() : null);
    context.documentLineSupport().ensureAtLineStart(doc);

    FilterEngine.Match match = null;
    if (meta != null) {
      match = context.filterRoutingSupport().matchFor(ref, meta, from, text);
      if (context.filterRoutingSupport().handleHiddenTextAppend(ref, from, text, meta, match)) {
        return;
      }
    }

    ChatTranscriptState state = context.state(ref);
    ChatTranscriptTextAppendSupport.appendVisibleLine(
        context.textAppendSupportContext(),
        ref,
        doc,
        state == null ? null : state.messageCatalog(),
        from,
        text,
        fromStyle,
        msgStyle,
        allowEmbeds,
        meta,
        match,
        tailComponent,
        tailAttrs,
        context.runtimeSettingsSupport().timestampsIncludeChatMessages(),
        context.runtimeSettingsSupport().timestampsIncludePresenceMessages(),
        context.shouldDeferRichTextDuringHistoryBatch(ref),
        enabled(context.imageEmbedsEnabled()),
        enabled(context.linkPreviewsEnabled()));
  }

  public int insertLineAt(
      Context context,
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      LineMeta meta) {
    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.document(ref);
    context.noteEpochMs().note(ref, meta != null ? meta.epochMs() : null);
    if (doc == null) return Math.max(0, insertAt);

    FilterEngine.Match match = null;
    if (meta != null) {
      match = context.filterRoutingSupport().matchFor(ref, meta, from, text);
      ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
          context
              .filterRoutingSupport()
              .handleHiddenTextHistoryInsert(ref, insertAt, from, text, meta, match);
      if (hidden.handled()) {
        return hidden.nextInsertAt();
      }
    }

    context.endFilteredInsertRun().accept(ref);
    ChatTranscriptState state = context.state(ref);
    return ChatTranscriptTextInsertSupport.insertVisibleLine(
        context.textInsertSupportContext(),
        ref,
        doc,
        state == null ? null : state.messageCatalog(),
        insertAt,
        from,
        text,
        fromStyle,
        msgStyle,
        meta,
        match,
        context.runtimeSettingsSupport().timestampsIncludeChatMessages(),
        context.runtimeSettingsSupport().timestampsIncludePresenceMessages(),
        context.shouldDeferRichTextDuringHistoryBatch(ref));
  }

  private static boolean enabled(BooleanSupplier supplier) {
    try {
      return supplier.getAsBoolean();
    } catch (Exception ignored) {
      return false;
    }
  }
}
