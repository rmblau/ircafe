package cafe.woden.ircclient.ui.chat.transcript.flow;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import javax.swing.text.StyledDocument;

/** Wraps filtered placeholder flow behind a single bound callback surface. */
public final class ChatTranscriptFilteredFlowCoordinator {

  private final ChatTranscriptFilteredFlowSupport support = new ChatTranscriptFilteredFlowSupport();
  private final ChatTranscriptFilteredLinesSupport filteredLinesSupport;
  private ChatTranscriptFilteredFlowSupport.Context context;

  public ChatTranscriptFilteredFlowCoordinator(
      ChatTranscriptFilteredLinesSupport filteredLinesSupport) {
    this.filteredLinesSupport =
        Objects.requireNonNull(filteredLinesSupport, "filteredLinesSupport");
  }

  public void bindContext(
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      ChatTranscriptFilteredFlowSupport.EnsureTargetExistsHandler ensureTargetExists,
      ChatTranscriptFilteredFlowSupport.EpochNoteHandler noteEpochMs,
      BooleanSupplier defaultDeferRichTextDuringHistoryBatch) {
    this.context =
        new ChatTranscriptFilteredFlowSupport.Context(
            filteredLinesSupport,
            filterRoutingSupport,
            docs,
            stateByTarget,
            ensureTargetExists,
            noteEpochMs,
            defaultDeferRichTextDuringHistoryBatch);
  }

  public void beginHistoryInsertBatch(TargetRef ref, boolean forceDeferRichText) {
    support.beginHistoryInsertBatch(requireBoundContext(), ref, forceDeferRichText);
  }

  public void endHistoryInsertBatch(TargetRef ref) {
    support.endHistoryInsertBatch(requireBoundContext(), ref);
  }

  public void endAppendRun(TargetRef ref) {
    support.endAppendRun(requireBoundContext(), ref);
  }

  public void endInsertRun(TargetRef ref) {
    support.endInsertRun(requireBoundContext(), ref);
  }

  public boolean shouldDeferRichTextDuringHistoryBatch(TargetRef ref) {
    return support.shouldDeferRichTextDuringHistoryBatch(requireBoundContext(), ref);
  }

  public void onFilteredLineAppend(
      TargetRef ref, String previewText, LineMeta hiddenMeta, FilterEngine.Match match) {
    support.onFilteredLineAppend(requireBoundContext(), ref, previewText, hiddenMeta, match);
  }

  public int onFilteredLineInsertAt(
      TargetRef ref,
      int insertAt,
      String previewText,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    return support.onFilteredLineInsertAt(
        requireBoundContext(), ref, insertAt, previewText, hiddenMeta, match);
  }

  public ChatTranscriptFilteredLinesSupport filteredLinesSupport() {
    return filteredLinesSupport;
  }

  private ChatTranscriptFilteredFlowSupport.Context requireBoundContext() {
    if (context == null) {
      throw new IllegalStateException("Filtered flow coordinator context not bound");
    }
    return context;
  }
}
