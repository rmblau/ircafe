package cafe.woden.ircclient.ui.chat.transcript.line;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.awt.Component;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

/** Owns the shared visible-line append/insert bridge. */
public final class ChatTranscriptLineLifecycleCoordinator {

  private final Object mutationLock;
  private final ChatTranscriptLineFlowSupport lineFlowSupport = new ChatTranscriptLineFlowSupport();
  private ChatTranscriptLineFlowSupport.Context lineFlowContext;

  public ChatTranscriptLineLifecycleCoordinator(Object mutationLock) {
    this.mutationLock = Objects.requireNonNull(mutationLock, "mutationLock");
  }

  public void bindContexts(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      Consumer<TargetRef> ensureTargetExists,
      ChatTranscriptLineFlowSupport.EpochNoteHandler noteEpochMs,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      Consumer<TargetRef> endFilteredInsertRun,
      Predicate<TargetRef> deferRichTextDuringHistoryBatch,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptTextAppendSupport.Context textAppendSupportContext,
      ChatTranscriptTextInsertSupport.Context textInsertSupportContext,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      BooleanSupplier imageEmbedsEnabled,
      BooleanSupplier linkPreviewsEnabled,
      ChatTranscriptLineFlowSupport.PendingHistoryDividerFlusher flushPendingHistoryDivider) {
    this.lineFlowContext =
        new ChatTranscriptLineFlowSupport.Context(
            docs,
            stateByTarget,
            ensureTargetExists,
            noteEpochMs,
            flushPendingHistoryDivider,
            filterRoutingSupport,
            endFilteredInsertRun,
            deferRichTextDuringHistoryBatch,
            documentLineSupport,
            textAppendSupportContext,
            textInsertSupportContext,
            runtimeSettingsSupport,
            imageEmbedsEnabled,
            linkPreviewsEnabled);
  }

  public void appendLine(
      TargetRef ref,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      boolean allowEmbeds,
      LineMeta meta) {
    synchronized (mutationLock) {
      lineFlowSupport.appendLine(
          requireLineFlowContext(),
          ref,
          from,
          text,
          fromStyle,
          msgStyle,
          allowEmbeds,
          meta,
          null,
          null);
    }
  }

  public void appendLineWithTail(
      TargetRef ref,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      LineMeta meta,
      Component tailComponent,
      AttributeSet tailAttrs) {
    synchronized (mutationLock) {
      lineFlowSupport.appendLine(
          requireLineFlowContext(),
          ref,
          from,
          text,
          fromStyle,
          msgStyle,
          true,
          meta,
          tailComponent,
          tailAttrs);
    }
  }

  public int insertLineAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      LineMeta meta) {
    synchronized (mutationLock) {
      return lineFlowSupport.insertLineAt(
          requireLineFlowContext(), ref, insertAt, from, text, fromStyle, msgStyle, meta);
    }
  }

  private ChatTranscriptLineFlowSupport.Context requireLineFlowContext() {
    if (lineFlowContext == null) {
      throw new IllegalStateException("Line coordinator context not bound");
    }
    return lineFlowContext;
  }
}
