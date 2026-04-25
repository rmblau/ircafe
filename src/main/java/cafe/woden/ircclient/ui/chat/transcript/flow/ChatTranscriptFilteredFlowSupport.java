package cafe.woden.ircclient.ui.chat.transcript.flow;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import javax.swing.text.StyledDocument;

/** Shared orchestration for filtered placeholder runs and history-batch rich-text deferral. */
public final class ChatTranscriptFilteredFlowSupport {

  @FunctionalInterface
  public interface EnsureTargetExistsHandler {
    void ensure(TargetRef ref);
  }

  @FunctionalInterface
  public interface EpochNoteHandler {
    void note(TargetRef ref, Long epochMs);
  }

  public record Context(
      ChatTranscriptFilteredLinesSupport filteredLinesSupport,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      EnsureTargetExistsHandler ensureTargetExists,
      EpochNoteHandler noteEpochMs,
      BooleanSupplier defaultDeferRichTextDuringHistoryBatch) {
    public Context {
      Objects.requireNonNull(filteredLinesSupport, "filteredLinesSupport");
      Objects.requireNonNull(filterRoutingSupport, "filterRoutingSupport");
      Objects.requireNonNull(docs, "docs");
      Objects.requireNonNull(stateByTarget, "stateByTarget");
      Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
      Objects.requireNonNull(noteEpochMs, "noteEpochMs");
      Objects.requireNonNull(
          defaultDeferRichTextDuringHistoryBatch, "defaultDeferRichTextDuringHistoryBatch");
    }

    StyledDocument document(TargetRef ref) {
      return docs.get(ref);
    }

    ChatTranscriptState state(TargetRef ref) {
      return stateByTarget.get(ref);
    }

    boolean defaultDeferRichTextDuringHistoryBatchEnabled() {
      return defaultDeferRichTextDuringHistoryBatch.getAsBoolean();
    }
  }

  public void endAppendRun(Context context, TargetRef ref) {
    if (ref == null) {
      return;
    }
    ChatTranscriptState state = context.state(ref);
    if (state != null) {
      context.filteredLinesSupport().endAppendRun(state.filteredLines());
    }
  }

  public void endInsertRun(Context context, TargetRef ref) {
    if (ref == null) {
      return;
    }
    ChatTranscriptState state = context.state(ref);
    if (state != null) {
      context.filteredLinesSupport().endInsertRun(state.filteredLines());
    }
  }

  public void beginHistoryInsertBatch(Context context, TargetRef ref, boolean forceDeferRichText) {
    if (ref == null) {
      return;
    }
    context.ensureTargetExists().ensure(ref);
    ChatTranscriptState state = context.state(ref);
    if (state != null) {
      context
          .filteredLinesSupport()
          .beginHistoryInsertBatch(state.filteredLines(), forceDeferRichText);
    }
  }

  public void endHistoryInsertBatch(Context context, TargetRef ref) {
    if (ref == null) {
      return;
    }
    ChatTranscriptState state = context.state(ref);
    if (state != null) {
      context.filteredLinesSupport().endHistoryInsertBatch(state.filteredLines());
    }
  }

  public boolean shouldDeferRichTextDuringHistoryBatch(Context context, TargetRef ref) {
    if (ref == null) {
      return false;
    }
    ChatTranscriptState state = context.state(ref);
    if (state == null
        || !context.filteredLinesSupport().historyInsertBatchActive(state.filteredLines())) {
      return false;
    }
    if (context
        .filteredLinesSupport()
        .forceDeferRichTextDuringHistoryBatch(state.filteredLines())) {
      return true;
    }
    try {
      return context.defaultDeferRichTextDuringHistoryBatchEnabled();
    } catch (Exception ignored) {
      return false;
    }
  }

  public void onFilteredLineAppend(
      Context context,
      TargetRef ref,
      String previewText,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    if (ref == null || match == null || !match.isHide()) {
      return;
    }
    context.ensureTargetExists().ensure(ref);
    context.noteEpochMs().note(ref, hiddenMeta != null ? hiddenMeta.epochMs() : null);

    StyledDocument doc = context.document(ref);
    ChatTranscriptState state = context.state(ref);
    context
        .filteredLinesSupport()
        .onFilteredLineAppend(
            ref,
            doc,
            state == null ? null : state.filteredLines(),
            context.filterRoutingSupport().effectiveFor(ref),
            previewText,
            hiddenMeta,
            match);
  }

  public int onFilteredLineInsertAt(
      Context context,
      TargetRef ref,
      int insertAt,
      String previewText,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    if (ref == null || match == null || !match.isHide()) {
      return Math.max(0, insertAt);
    }
    context.ensureTargetExists().ensure(ref);
    context.noteEpochMs().note(ref, hiddenMeta != null ? hiddenMeta.epochMs() : null);

    StyledDocument doc = context.document(ref);
    ChatTranscriptState state = context.state(ref);
    return context
        .filteredLinesSupport()
        .onFilteredLineInsertAt(
            ref,
            doc,
            state == null ? null : state.filteredLines(),
            context.filterRoutingSupport().effectiveFor(ref),
            insertAt,
            previewText,
            hiddenMeta,
            match);
  }
}
